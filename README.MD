## Groovy code execution service tests

### Requirements
* Specify version of the service by setting up environment variable `TEST_IMAGE` 
(or default value `behavox/product-qa-groovy-1.0.1:latest` will be used)
* [Install alure framework](https://docs.qameta.io/allure/#_get_started)

### Run test: 
```shell 
./gradlew clean test
```

### Generation report

```shell 
allure serve allure-results
```