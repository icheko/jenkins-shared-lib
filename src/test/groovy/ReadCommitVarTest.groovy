import org.junit.*
import com.lesfurets.jenkins.unit.*
import static groovy.test.GroovyAssert.*

class ReadCommitVarTest extends BasePipelineTest {
    def readCommitVar

    @Before
    void setUp() {
        super.setUp()
        readCommitVar = loadScript("vars/readCommitVar.groovy")
    }

    @Test
    void testCaseParameterNoValue() {
        def shResult = "US1000 - Test Case \\deploy"

        // create mock sh step
        helper.registerAllowedMethod("sh", [ Map ]) { shResult }

        def result = readCommitVar('deploy')
        assertEquals "result:", "true", result
    }

    @Test
    void testCaseParameterWithTValue() {
        def shResult = "US1000 - Test Case \\deploy:t"

        // create mock sh step
        helper.registerAllowedMethod("sh", [ Map ]) { shResult }

        def result = readCommitVar('deploy')
        assertEquals "result:", "true", result
    }

    @Test
    void testCaseParameterWithAnyValue() {
        def shResult = "US1000 - Test Case \\deploy:anyvalue"

        // create mock sh step
        helper.registerAllowedMethod("sh", [ Map ]) { shResult }

        def result = readCommitVar('deploy')
        assertEquals "result:", "anyvalue", result
    }

    @Test
    void testCaseParameterNotFound() {
        def shResult = "US1000 - Test Case"
        def method = readCommitVar

        // create mock sh step
        helper.registerAllowedMethod("sh", [ Map ]) { shResult }

        def result = shouldFail MissingPropertyException, {
            method('deploy', 'JobParam')
        }

        assert result.message == 'No such property: params for class: readCommitVar'
    }
 
    @Test
    void testCaseParameterNotFoundOtherParametersDefined() {
        def shResult = "US1000 - Test Case \\var1:value \\var2:value"
        def method = readCommitVar

        // create mock sh step
        helper.registerAllowedMethod("sh", [ Map ]) { shResult }

        def result = shouldFail MissingPropertyException, {
            method('deploy', 'JobParam')
        }

        assert result.message == 'No such property: params for class: readCommitVar'
    }
}