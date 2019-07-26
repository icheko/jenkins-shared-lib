# jenkins-shared-library
A shared library for Jenkins

## Test Suite
Install docker and add this entry to your ~/.bash_profile

```
function gradle(){
    rm -Rfv .gradle/ && docker run --rm -u gradle -v "$PWD":/home/gradle/project -w /home/gradle/project gradle gradle "$@"
}
```

Run gradle test

[![asciicast](https://asciinema.org/a/259454.svg)](https://asciinema.org/a/259454)