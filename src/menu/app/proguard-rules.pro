# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/rivergod/bin/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontwarn com.fasterxml.jackson.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.jaxen.**
-dontwarn javax.xml.stream.**
-dontwarn org.simpleframework.xml.**
-dontwarn org.apache.tools.ant.Task
-dontwarn org.apache.tools.ant.BuildException
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString
