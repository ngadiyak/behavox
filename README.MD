## Groovy code execution service tests

### Requirements
* Specify version of the service by setting up environment variable `TEST_IMAGE` 
(or default value `behavox/product-qa-groovy-1.0.1:latest` will be used)

### Run test:
* Download docker image from https://www.dropbox.com/s/5pe3eu55dk3awp8/product-qa-groovy-1.0.1.tar?dl=0
* Load docker image
```shell
docker load < product-qa-groovy-1.0.1.tar
````
* Launch compile and test execution
```shell 
./gradlew clean test
```

### Report
Report will be placed into ```./build/reports/tests``` after execution