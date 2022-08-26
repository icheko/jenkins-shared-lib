/*
This function merges a list of Kubernetes Pod
templates defined in a shared library to create
a single Pod definition for the build job. Without this
function, the agent definition would have to be defined
for each stage.

Usage example:

// load shared library via @Library or other methods
pipeline {
  agent {
    kubernetes {
        yaml mergePodTemplates('node-10,sonar-scanner,veracode')
    }
  }
  stages {
    stage('Build App'){
      steps {
        container('node-10'){
          sh 'npm install'
        }
      }
    }
    stage('SonarQube Analysis') {
      steps {
        container('sonar-scanner'){
          // scan
        }
      }
    }
    stage('Veracode Analysis') {
      steps {
        container('veracode'){
          // scan
        }
      }
    }
  } //  stages
}
*/

import org.csanchez.jenkins.plugins.kubernetes.PodTemplate  
import org.csanchez.jenkins.plugins.kubernetes.pod.yaml.Merge
import static org.csanchez.jenkins.plugins.kubernetes.PodTemplateUtils.combine
import org.csanchez.jenkins.plugins.kubernetes.PodTemplateBuilder
import org.csanchez.jenkins.plugins.kubernetes.KubernetesSlave
import org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.utils.Serialization

def addYamlExt(String string)  {
    return string.endsWith(".yaml") ? string : string + ".yaml"
}

def call(String yamlFiles){

    yamlFiles = yamlFiles.split(',')

    String parentYaml = addYamlExt(yamlFiles[0])
    // println "ParentFile: "+parentYaml
    parentYaml = libraryResource(parentYaml)
    PodTemplate parent = new PodTemplate()
    parent.setYaml(parentYaml)

    for (i = 1; i < yamlFiles.size(); i++) {
        String childYaml = addYamlExt(yamlFiles[i])
        // println "ChildFile: "+childYaml
        childYaml = libraryResource(childYaml)

        // println "Parent ---\n" + parent
        // println "ChildYaml ---\n" + childYaml
        
        PodTemplate child = new PodTemplate()
        child.setYaml(childYaml)
        child.setYamlMergeStrategy(new Merge())
        child.setInheritFrom("parent")

        PodTemplate result = combine(parent, child)
        parent = result
    }
    
    KubernetesCloud cloud = Jenkins.get().getCloud("kubernetes")
    KubernetesSlave ks = new KubernetesSlave.Builder().cloud(cloud).podTemplate(parent).build()
    Pod pod = new PodTemplateBuilder(parent, ks).build()
    String yaml = Serialization.asYaml(pod)
    println "Combined\n" + yaml

    return yaml
}
