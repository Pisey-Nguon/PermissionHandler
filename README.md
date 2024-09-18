# Welcome to PermissionHandler!

Hi! This is an Android Library that provides a feature for the developer to handle permission in android so please take a look at the instructions below, Thank!


# How to
[![](https://jitpack.io/v/Pisey-Nguon/PermissionHandler.svg)](https://jitpack.io/#Pisey-Nguon/PermissionHandler)
To get a Git project into your build:

**Step 1.**  Add the JitPack repository to your build file
```css
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
**Step 2.** Add the dependency
```css
	dependencies {
	        implementation 'com.github.Pisey-Nguon:PermissionHandler:TAG'
	}
```
Note: TAG is a version of the package so please change it based here [![](https://jitpack.io/v/Pisey-Nguon/PermissionHandler.svg)](https://jitpack.io/#Pisey-Nguon/PermissionHandler)

**Step 3.** Implementation example
```css
        requestTakePhoto {
            view?.findViewById<AppCompatImageView>(R.id.imageCamera)?.setImageURI(Uri.parse(it))
        }
        currentLocation { location ->
            view.findViewById<TextView>(R.id.txtCurrentLocation).text = location.toString()
        }
```
Note: Don't forget to put permissions on manifest and add file_paths.xml if using with function take photo
## UML diagrams

This is a behavior of PermissionHandler

![enter image description here](https://github.com/Pisey-Nguon/PermissionHandler/raw/main/PermissionHandler.png)
