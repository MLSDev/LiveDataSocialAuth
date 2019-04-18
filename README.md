[![License](https://img.shields.io/github/license/mashape/apistatus.svg)](https://opensource.org/licenses/MIT)
[![Download](https://api.bintray.com/packages/spetrosiukmlsdev/mlsdev/livedatasocialauth/images/download.svg)](https://bintray.com/spetrosiukmlsdev/mlsdev/livedatasocialauth/_latestVersion)

# LiveDataSocialAuth
LiveDataSocialAuth is a library for the signing in with Google or Facabook accounts

## Setup
### Gradle file
To use this library your `minSdkVersion` must be >= 21.

In your build.gradle :
```gradle
dependencies {
    implementation "com.mlsdev.livedatasocialauth:library:$latestVersion"
}
```

### Google Sign-In
To do all needed stuff follow the oficial [documentation](https://developers.google.com/identity/sign-in/android/start-integrating)
#### Key points
- Genrate and add the `google-services.json` into the `/app` directory 
- Add the dependency `classpath 'com.google.gms:google-services:4.2.0'` to your project-level `build.gradle`
- Add the plugin `apply plugin: 'com.google.gms.google-services'` at the end of your app-level `build.gradle`
- Add the dependency `implementation 'com.google.android.gms:play-services-auth:16.0.1'` to your app-level `build.gradle`

### Facebook Login
To do all needed stuff follow the oficial [documentation](https://developers.facebook.com/docs/facebook-login/android/)
#### Key points
- Add your facebook application `id` into the `strings.xml` file:
```xml
  <string name="facebook_app_id">your_application_id</string>
```
- Add the `meta-data` inside your `application` element into your app `manifest.xml` file:
```xml
  <meta-data 
        android:name="com.facebook.sdk.ApplicationId" 
        android:value="@string/facebook_app_id"/>
```

## Usage
###

## Authors
* [Sergey Petrosyuk](mailto:petrosyuk@mlsdev.com), MLSDev 

## About MLSDev

[<img src="https://cloud.githubusercontent.com/assets/1778155/11761239/ccfddf60-a0c2-11e5-8f2a-8573029ab09d.png" alt="MLSDev.com">][mlsdev]

LiveDataSocialAuth is maintained by MLSDev, Inc. We specialize in providing all-in-one solution in mobile and web development. Our team follows Lean principles and works according to agile methodologies to deliver the best results reducing the budget for development and its timeline.

Find out more [here][mlsdev] and don't hesitate to [contact us][contact]!

[mlsdev]: http://mlsdev.com
[contact]: http://mlsdev.com/contact_us
[github-frederikos]: https://github.com/SerhiyPetrosyuk
