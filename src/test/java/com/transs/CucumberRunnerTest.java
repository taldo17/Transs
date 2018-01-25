package com.transs;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)

@CucumberOptions
        (
                format = { "pretty","json:cucumber/test-report/SearchProject Reports/cucumber.json"},
                features ={ "classpath:features"}
        )


public class CucumberRunnerTest
{

}
