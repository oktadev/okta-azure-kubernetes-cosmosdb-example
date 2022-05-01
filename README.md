# Azure Cosmos DB + Kubernetes + JHipster Example

This example uses a microservice network generated with JHipster 7. It includes four applications: a JHipster registry, an application gateway, a store resource server, and a blog resource server. The store uses an Azure Cosmos DB API for Mongo DB as a persistence store. Take a look at [the blog post]() for this repository for more information.

This project is based on two of Matt Raible's tutorials: [Reactive Java Microservices with Spring Boot and JHipster](https://developer.okta.com/blog/2021/01/20/reactive-java-microservices) and [Kubernetes to the Cloud with Spring Boot and JHipster](https://developer.okta.com/blog/2021/06/01/kubernetes-spring-boot-jhipster). In these tutorials, he builds a reactive Java microservice and shows how to deploy it to Google Cloud (GCP). I have modified the project to work with Azure and Cosmos DB.

**Prerequisites:**

- [Docker](https://docs.docker.com/get-docker/): you'll need to have both **Docker Engine** and **Docker Compose** installed (If you install the docker desktop, this will automatically install both. On Linux, if you install Docker Engine individually, you will have to also [install Docker Compose](https://docs.docker.com/compose/install/)) separately.
- [Java 11](https://adoptopenjdk.net/): this post requires Java 11. If you need to manage multiple Java versions, SDKMAN! is a good solution. Check out [their docs to install it](https://sdkman.io/installit).
- [Okta CLI](https://cli.okta.com/manual/#installation)
- [Azure Cloud account](https://azure.microsoft.com/en-us/free/): they offer a free-tier account with a $200 credit to start
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)

**Table of Contents**

* [Clone the Project](#clone-the-project)
* [Create the Azure Cosmos DB database](#create-the-azure-cosmos-db-database)
* [Configure Okta OAuth](#configure-okta-oath)
* [Build the Docker Images and Run App](build-the-docker-images-and-run-app)
* [Links](#links)
* [Help](#help)
* [License](#license)

## Clone the Project

To install this example, run the following commands:

```bash
git clone https://github.com/oktadev/<repo-name>.git
cd <repo-name>
```

## Create the Azure Cosmos DB database

Log into the Azure CLI using a Bash shell. 

```bash
az login
```

This should show you the subscriptions for your account. The default subscription name for me was `Azure subscription 1`.

```bash
[
  {
    "cloudName": "AzureCloud",
    "homeTenantId": "21c44b6d-a007-4d48-80cb-c45966ca1af9",
    "id": "90eb9f51-b4be-4a9f-a69f-11b7668a874d",
    "isDefault": true,
    "name": "Azure subscription 1",
    ...
  }
]
```

Make sure your subscription is set to the default.

```bash
az account set --subscription <you-subscription-name>
```

Create a resource group with the following command.

```bash
az group create --name australia-east --location australiaeast
```

Create the Cosmos DB account in the resource group. Substitute your Azure subscription name in the command below.

```bash
az cosmosdb create --name jhipster-cosmosdb --resource-group australia-east --kind MongoDB --subscription <you-subscription-name> --enable-free-tier true --enable-public-network true
```

If you get an error that says`(BadRequest) DNS record for cosmosdb under zone Document is already taken.`, you need to change the `--name` parameter to something else. Since this is used to generate the public URI for the database it needs to be unique across Azure. Try adding your name or a few random numbers.

This may take a few minutes.

Retrieve the primary Mongo DB connection string using the following command. **If you changed the database name above, you will need to update it in the command below.**

```bash
az cosmosdb keys list --type connection-strings --name jhipster-cosmosdb --resource-group australia-east
```

Add a `.env` file in the `docker-compose` subdirectory. It needs to contain this connection string (`SPRING_DATA_MONGO_URI`).

`docker-compose/.env`

```env
SPRING_DATA_MONGO_URI="<your-connection-string>"
```

## Configure Okta OAuth

If you already have an Okta account, use `okta login` to log into that account with the CLI. Otherwise, use `okta register` to sign up for a free account. 

Create the OIDC app using the following command using a Bash shell opened to the project root.

```bash
okta apps create jhipster
```

You can accept the default values by pressing **enter**. 

This creates `.okta.env`. 

Use the values from `.okta.env` to fill in the `issuer-uri`, `client-id`, and `client-secret` in the central server config `application.yml` file.

`docker-compose/central-server-config/application.yml`

```yaml
spring:
  security:
    oauth2:
      client:
        provider:
          oidc:
            issuer-uri: https://<your-okta-uri/oauth2/default
        registration:
          oidc:
            client-id: <your-client-id>
            client-secret: "{cipher}<your-encrypted-key>"
```

Ignore the stuff about cipher and encryption for the `client-secret`. Just for the purposes of getting the app running, put your client secret there. However, **leaving secrets in config files that are checked into repositories is a security risk**. The tutorial shows you how to avoid this. 

## Build the Docker Images and Run App

Build the docker image for each of the projects: `gateway`, `store`, and `blog`.

 In the three different app directories, run the following Gradle command.

```
./gradlew -Pprod bootJar jibDockerBuild
```

Navigate to the `docker-compose` directory and run the app.

```bash
docker-compose up
```

Give that a minute or two to finish running all the services.

You can check out the registry at:  http://localhost:8761/

You'll be directed to the Okta login form to sign in.

Once all the services are green, open the gateway service: http://localhost:8080/

## Cleaning Up

You can delete the Azure resource group, which includes the database.

```bash
az group delete --name australia-east --no-wait --yes
```

You can delete all the resources created by docker compose with the following command (run from the `docker-compose` directory).

```bash
 docker-compose down -v --remove-orphans
```

And remove the local images:

```bash
docker image rm blog store gateway
```

## Links

This example uses the following open source libraries:

* [Spring Boot](https://spring.io/projects/spring-boot)
* [Spring Cloud](https://spring.io/projects/spring-cloud)
* [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
* [Spring Security](https://spring.io/projects/spring-security)
* [JHipster](https://www.jhipster.tech)
* [OpenJDK](https://openjdk.java.net/)
* [K9s](https://k9scli.io/)

## Help

Please post any questions as comments on [this example's blog post][blog], or on the [Okta Developer Forums](https://devforum.okta.com/).

## License

Apache 2.0, see [LICENSE](LICENSE).

[blog]: https://developer.okta.com/blog/2021/06/01/kubernetes-spring-boot-jhipster
