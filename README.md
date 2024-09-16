# Virgil Pythia Java SDK
[![Build Status](https://api.travis-ci.org/VirgilSecurity/pythia-java.svg?branch=master)](https://travis-ci.org/VirgilSecurity/pythia-java)
[![GitHub license](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg)](https://github.com/VirgilSecurity/virgil/blob/master/LICENSE)


[Introduction](#introduction) | [SDK Features](#sdk-features) | [Install and configure SDK](#install-and-configure-sdk) | [Usage Examples](#usage-examples) | [Docs](#docs) | [Support](#support)

## Introduction

<a href="https://developer.virgilsecurity.com/docs"><img width="230px" src="https://cdn.virgilsecurity.com/assets/images/github/logos/virgil-logo-red.png" align="left" hspace="10" vspace="6"></a>[Virgil Security](https://virgilsecurity.com) provides an SDK which allows you to communicate with Virgil Pythia Service and implement Pythia protocol for the following use cases: 
- **Breach-proof password**. Pythia is a technology that gives you a new, more secure mechanism that "breach-proofs" user passwords and lessens the security risks associated with weak passwords by providing cryptographic leverage for the defender (by eliminating offline password cracking attacks), detection for online attacks, and key rotation to recover from stolen password databases.
- **BrainKey**. User's Private Key which is based on user's password. BrainKey can be easily restored and is resistant to online and offline attacks.

In both cases you get the mechanism which assures you that neither Virgil nor attackers know anything about user's password.

## SDK Features
- communicate with Virgil Pythia Service
- manage your Pythia application credentials
- create, verify and update user's breach-proof password
- generate user's BrainKey
- use [Virgil Crypto Pythia library][_virgil_crypto_pythia]

## Install and configure SDK

The Virgil Java SDK is provided as a package named com.virgilsecurity.sdk. The package is distributed via Maven repository.

The package is available for:
- Java 7 and newer
- Android API 16 and newer

Prerequisites:
- Java Development Kit (JDK) 7+
- Maven 3+

You can easily add SDK dependency to your project, just follow the examples below.

### Install SDK Package

#### Maven

[Apache Maven](https://maven.apache.org/) is a software project management and comprehension tool.

To integrate Virgil SDK into your Java project using Maven, set up dependencies in your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.virgilsecurity</groupId>
        <artifactId>pythia</artifactId>
        <version>0.2.1</version>
    </dependency>
</dependencies>
```

#### Gradle

[Gradle](https://gradle.org/) is an open-source build automation system that builds upon the concepts of Apache Ant and Apache Maven and introduces a Groovy-based domain-specific language (DSL) instead of the XML form used by Apache Maven for declaring the project configuration.

##### Server

To integrate Virgil SDK into your Java project using Gradle, set up dependencies in your `build.gradle`:

```
dependencies {
    compile 'com.virgilsecurity:pythia:0.2.1'
}
```

##### Android

To integrate Virgil SDK into your Android project using Gradle, add jcenter() repository if missing:

```
repositories {
    jcenter()
}
```

Set up dependencies in your `build.gradle`:

```
dependencies {
    implementation 'com.virgilsecurity.sdk:crypto-android:5.0.8@aar'
    implementation ('com.virgilsecurity:pythia:0.2.1') {
        exclude group: 'com.virgilsecurity.sdk', module: 'crypto'
    }
}
```

### Configure SDK

When you create a Pythia Application on the [Virgil Dashboard](https://dashboard.virgilsecurity.com/) you will receive Application credentials including: Proof Key and App ID. Specify your Pythia Application and Virgil account credentials in a Pythia SDK class instance.
These credentials are used for the following purposes:
- generating a JWT token that is used for authorization on the Virgil Services
- creating a user's breach-proof password

Here is an example of how to specify your credentials SDK class instance:
```java
// here set your Virgil Account and Pythia Application credentials
PythiaContext context = new PythiaContext.Builder()
    .setAppId("APP_ID")
    .setApiPublicKeyIdentifier("API_KEY_ID")
    .setApiKey("API_KEY")
    .setProofKeys(Arrays.asList("PK.1.PROOF_KEY"))
    .setPythiaCrypto(new VirgilPythiaCrypto())
    .build();

Pythia pythia = new Pythia(context);
```


## Usage Examples

### Breach-proof password

Virgil Pythia SDK lets you easily perform all the necessary operations to create, verify and update user's breach-proof password without requiring any additional actions and use Virgil Crypto library.

First of all, you need to set up your database to store users' breach-proof passwords. Create additional columns in your database for storing the following parameters:
<table class="params">
<thead>
		<tr>
			<th>Parameters</th>
			<th>Type</th>
			<th>Size (bytes)</th>
			<th>Description</th>
		</tr>
</thead>

<tbody>
<tr>
	<td>salt</td>
	<td>blob</td>
	<td>32</td>
	<td> Unique random string that is generated by Pythia SDK for each user</td>
</tr>

<tr>
	<td>deblindedPassword</td>
	<td>blob </td>
	<td>384 </td>
	<td>user's breach-proof password</td>
</tr>

<tr>
	<td>version</td>
	<td>int </td>
	<td>4 </td>
	<td>Version of your Pythia Application credentials. This parameter has the same value for all users unless you generate new Pythia credentials on Virgil Dashboard</td>
</tr>

</tbody>
</table>

Now we can start creating breach-proof passwords for users. Depending on the situation, you will use one of the following Pythia SDK functions:
- `createBreachProofPassword` is used to create a user's breach-proof password on your Application Server.
- `verifyBreachProofPassword` is used to verify a user's breach-proof password.

#### Create Breach-Proof Password

Use this flow to create a new breach-proof password for a user.

> Remember, if you already have a database with user passwords, you don't have to wait until a user logs in into your system to implement Pythia. You can go through your database and create breach-proof user passwords at any time.

So, in order to create a user's breach-proof password for a new database or available one, go through the following operations:
- Take a user's password (or its hash or whatever you use) and pass it into a `CreateBreachProofPassword` function in SDK on your Server side.
- Pythia SDK generates unique user's **salt** and **version** (which is the same for every user until you change your app credentials on Pythia Service). You need to store user's salt and version in your database in associated columns created on previous step.
- Pythia SDK blinds a password and sends a request to Pythia Service to get a **transformed blinded password**.
- Pythia SDK de-blinds the transformed blinded password into a user's **deblinded password** (that we call **breach-proof password**).

```java
// create a new Breach-proof password using user's password or its hash
BreachProofPassword pwd = pythia.createBreachProofPassword("USER_PASSWORD");

// save Breach-proof password parameters into your users DB
```

After performing `createBreachProofPassword` function you get previously mentioned parameters (`salt`, `deblindedPassword`, `version`), save these parameters into corresponding columns in your database.

Check that you updated all database records and delete the now unnecessary column where user passwords were previously stored.

#### Verify Breach-Proof Password

Use this flow when a user already has his or her own breach-proof password in your database. You will have to pass his or her password into an `verifyBreachProofPassword` function:

```java
// get user's Breach-proof password parameters from your users DB

// ...

// calculate user's Breach-proof password parameters
// compare these parameters with parameters from your DB
boolean isValid = pythia.verifyBreachProofPassword("USER_PASSWORD", pwd, true);

if (!isValid) {
    throw new Exception("Authentication failed");
}
```

The difference between the `verifyBreachProofPassword` and `createBreachProofPassword` functions is that the verification of Pythia Service is optional in `verifyBreachProofPassword` function, which allows you to achieve maximum performance when processing data. You can turn on a proof step in `verifyBreachProofPassword` function if you have any suspicions that a user or Pythia Service were compromised.

#### Update breach-proof passwords

This step will allow you to use an `updateToken` in order to update users' breach-proof passwords in your database.

> Use this flow only if your database was COMPROMISED.

How it works:
- Access your [Virgil Dashboard][_dashboard] and press the "My Database Was Compromised" button.
- Pythia Service generates a special updateToken and new Proof Key.
- You then specify new Pythia Application credentials in the Pythia SDK on your Server side.
- Then you use `UpdateBreachProofPassword` function to create new breach-proof passwords for your users (you don't need to regenerate user's password).
- Finally, you save the new breach-proof passwords into your database.

Here is an example of using the `updateBreachProofPassword` function:
```java
// get previous user's verifyBreachProofPassword parameters from a compromised DB

// ...

// set up an updateToken that you got on the Virgil Dashboard
// update previous user's Breach-proof password, and save new one into
// your DB

BreachProofPassword updatedPwd =
    pythia.updateBreachProofPassword("UT.1.2.UPDATE_TOKEN", pwd);
```
### BrainKey

*PYTHIA* Service can be used directly as a means to generate strong cryptographic keys based on user's **password** or other secret data. We call these keys the **BrainKeys**. Thus, when you need to restore a Private Key you use only user's Password and Pythia Service.

In order to create a user's BrainKey, go through the following operations:
- Register your E2EE application on [Virgil Dashboard][_dashboard] and get your app credentials
- Generate your API key or use available
- Set up **JWT provider** using previously mentioned parameters (**App ID, API key, API key ID**) on the Server side
- Generate JWT token with **user's identity** inside and transmit it to Client side (user's side)
- On Client side set up **access token provider** in order to specify JWT provider
- Setup BrainKey function with access token provider and pass user's password
- Send BrainKey request to Pythia Service
- Generate a strong cryptographic keypair based on a user's password or other user's secret


#### Generate BrainKey based on user's password
```java
// 1. Specify your JWT provider

// Get generated token from server-side
final String authenticatedQueryToServerSide = "eyJraWQiOiI3MGI0NDdlMzIxZjNhMGZkIiwidHlwIjoiSldUIiwiYWxnIjoiVkVEUzUxMiIsImN0eSI6InZpcmdpbC1qd3Q7dj0xIn0.eyJleHAiOjE1MTg2OTg5MTcsImlzcyI6InZpcmdpbC1iZTAwZTEwZTRlMWY0YmY1OGY5YjRkYzg1ZDc5Yzc3YSIsInN1YiI6ImlkZW50aXR5LUFsaWNlIiwiaWF0IjoxNTE4NjEyNTE3fQ.MFEwDQYJYIZIAWUDBAIDBQAEQP4Yo3yjmt8WWJ5mqs3Yrqc_VzG6nBtrW2KIjP-kxiIJL_7Wv0pqty7PDbDoGhkX8CJa6UOdyn3rBWRvMK7p7Ak";

// Setup AccessTokenProvider
AccessTokenProvider accessTokenProvider = new CachingJwtProvider(new RenewJwtCallback() {

    @Override
    public Jwt renewJwt(TokenContext tokenContext) {
        return new Jwt(authenticatedQueryToServerSide);
    }
});

// 2. Setup BrainKey

BrainKeyContext brainKeyContext = new BrainKeyContext.Builder()
        .setAccessTokenProvider(accessTokenProvider)
        .setPythiaCrypto(new VirgilPythiaCrypto())
        .setPythiaClient(new VirgilPythiaClient())
        .build();
BrainKey brainKey = new BrainKey(brainKeyContext);

// Generate default public/private keypair which is Curve ED25519
// If you need to generate several BrainKeys for the same password,
// use different IDs (optional). Default brainKeyId value is null.
VirgilKeyPair keyPair = brainKey.generateKeyPair("Your password", "Optional BrainKey id");
```

#### Generate BrainKey based on unique URL
The typical BrainKey implementation uses a password or concatenated answers to security questions to regenerate the user’s private key. But a unique session link generated by the system admin can also do the trick.

This typically makes the most sense for situations where it’s burdensome to require a password each time a user wants to send or receive messages, like single-session chats in a browser application.

Here’s the general flow of how BrainKey can be used to regenerate a private key based on a unique URL:
- When the user is ready to begin an encrypted messaging session, the application sends the user an SMS message
- The SMS message contains a unique link like https://healthcare.app/?session=abcdef13803488
- The string 'abcdef13803488' is used as a password for the private key regeneration
- By clicking on the link, the user immediately establishes a secure session using their existing private key regenerated with Brainkey and does not need to input an additional password

Important notes for implementation:
- The link is one-time use only. When the user clicks on the link, the original link expires and cannot be used again, and so a new link has to be created for each new chat session.
- All URL links must be short-lived (recommended lifetime is 1 minute).
- The SMS messages should be sent over a different channel than the one the user will be using for the secure chat session.
- If you’d like to add additional protection to ensure that the person clicking the link is the intended chat participant, users can be required to submit their name or any other security question. This answer will need to be built in as part of the BrainKey password.

```go
...
    VirgilKeyPair keyPair = brainKey.generateKeyPair("abcdef13803488", "Optional User SSN");
...
```
> Note! if you don't need to use additional parameters, like "Optional User SSN", you can just omit it: `VirgilKeyPair keyPair = brainKey.generateKeyPair("abcdef13803488");`


## Docs
Virgil Security has a powerful set of APIs, and the documentation below can get you started today.

* [Breach-Proof Password][_pythia_use_case] Use Case
* [Brain Key][_brain_key_use_case] Use Case
* [The Pythia PRF Service](https://eprint.iacr.org/2015/644.pdf) - foundation principles of the protocol
* [Virgil Security Documentation][_documentation]

## License

This library is released under the [3-clause BSD License](LICENSE).

## Support
Our developer support team is here to help you. Find out more information on our [Help Center](https://help.virgilsecurity.com/).

You can find us on [Twitter](https://twitter.com/VirgilSecurity) or send us email support@VirgilSecurity.com.

Also, get extra help from our support team on [Slack](https://virgilsecurity.com/join-community).

[_virgil_crypto_pythia]: https://github.com/VirgilSecurity/pythia
[_brain_key_use_case]: https://developer.virgilsecurity.com/docs/use-cases/v1/brainkey
[_pythia_use_case]: https://developer.virgilsecurity.com/docs/java/use-cases/v1/breach-proof-password
[_documentation]: https://developer.virgilsecurity.com/
[_dashboard]: https://dashboard.virgilsecurity.com/
