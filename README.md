This code shows sample code to do role assumption and submission to kinesis stream through AWS SDK 2.x

Mostly it provides work around for an error case where httpClient needs to be provided explcitly, because of dependent jars altering the default behaviour.

Shows how to assume IAM ROLE in KCL 2.x and submit records to kinesis stream.
Also high lights how to bye pass existing issue in AWS SDK ( through some transient dependency ).

NOTE :: In some sequence mostly related to dependent jar behaviour, if explicit httpClient is not
        set, then below error occurs ::

Error: Unable to load an HTTP implementation from any provider in the chain.You must declare a dependency on an appropriate HTTP implementation or pass in an SdkHttpClient explicitly to the client builder.
          
1. With attached pom.xml it works just file, however when multiple different AWS SDK packages
                      are included or through other transient dependencies, this erorr occurs.
2. By explcit setting httpClient, it can be work around until AWS fixes the issue.
