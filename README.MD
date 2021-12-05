## Groovy code execution service tests

### Requirements
* Specify version for test set up environment variable TEST_IMAGE (or tag `behavox/product-qa-groovy-1.0.1:latest` will be used )
* [Install alure framework](https://docs.qameta.io/allure/#_get_started)

### Run test: 
```shell 
./gradlew clean test
```

### Generation report

```shell 
allure serve allure-results
```