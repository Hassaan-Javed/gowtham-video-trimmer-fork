[![](https://jitpack.io/v/Hassaan-Javed/gowtham-video-trimmer-fork.svg)](https://jitpack.io/#Hassaan-Javed/gowtham-video-trimmer-fork)

# Android Video Trimmer (Fork)

_Forked from [a914‑gowtham/android‑video‑trimmer](https://github.com/a914‑gowtham/android‑video‑trimmer) (Apache 2.0)._

*This version removes FFmpeg‑Kit and uses LiTr instead*

# Android-video-trimmer

##### Helps to trim local videos with many customizations on Android applications using exoplayer2 and LiTr

![](https://github.com/a914-gowtham/Android-video-trimmer/blob/master/demo.gif)

## How to use
*For a working implementation, please have a look at the Sample Project*

1. Include the library as local library project.

+ Add the dependency to your app `build.gradle` file
+ Take a look at light weight version of this library [Android-video-trimmer-litr](https://github.com/a914-gowtham/android-video-trimmer-litr)
 ```gradle
 // replace x.y.z with latest available jitpack version
 dependencies {
    implementation 'com.github.Hassaan-Javed:gowtham-video-trimmer-fork:{LatestVersion}'
 }
 ```
 + Add to project's root `build.gradle` file:
```gradle
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}
```
2. Create a global variable for ActivityResultLauncher

```java
    //Java
    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK &&
                        result.getData() != null) {
                    Uri uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.getData()));
                    Log.d(TAG, "Trimmed path:: " + uri);
                   
                } else
                    LogMessage.v("videoTrimResultLauncher data is null");
            });
```

```kotlin
    //Kotlin
    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { 
    result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK &&
                        result.getData() != null) {
                Uri uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.getData()))
                Log.d(TAG, "Trimmed path:: " + uri)
         }else 
          	LogMessage.v("videoTrimResultLauncher data is null");
    }   
```

3. Add the code for opening Trim Activity.
```java
TrimVideo.activity(String.valueOf(videoUri))
          .setHideSeekBar(true)
          .start(this,startForResult);
```



## Using Different Modes (Ordered By Output Speed):

#### Default Mode:
```java
TrimVideo.activity(String.valueOf(videoUri))
          .setHideSeekBar(true)
          .start(this,startForResult);
```
   1. Fastest processing, No losses in quality((no compression), will be low accurate(2-3 secs)

#### Accuracy Mode:
```java
TrimVideo.activity(String.valueOf(videoUri))
          .setAccurateCut(true) 
          .setHideSeekBar(true)
          .start(this,startForResult);
```
   1. Faster processing, No losses in quality(no compression), accurate trimming. 


#### Video Compress Mode:
```java
.setCompressOption(new CompressOption(frameRate,bitRate,width,height))  //pass empty constructor for default compressoption
```
  * `FrameRate` Recommeded frameRate is 30
  * `BitRate`   Bitrate Can be between 150k to 1000k or 1M to 10M.Lower bitrate can reduce the quality and size of the video.
               Use 1M for decent quality output 
  * `Width` Width of the video output video. 
  * `Height` Height of the video output video.Use `TrimmerUtils.getVideoWidthHeight(this,Uri.parse(videoUri));` method to get the width and height of the video
   1. Video compressing process will take more time and duration will be accurate
```java
TrimVideo.activity(String.valueOf(videoUri))
          .setCompressOption(new CompressOption(30,"1M",460,320)) //pass empty constructor for default compress values
          .setHideSeekBar(true)
          .start(this,startForResult);
//You could divide the width and height by 2. when try to compress a large resolution videos ex:Taken from camera
/*int[] wAndh=TrimmerUtils.getVideoWidthHeight(this,Uri.parse(videoUri));
    int width=wAndh[0];
    int height=wAndh[1];
    if(wAndh[0]>800){
      width/=2;
      width/=2;
     .setCompressOption(new CompressOption(30,"1M",width,height))   
     }else
     .setCompressOption(new CompressOption(30,"400k",width,height))   
   */
```

## Customization

#### Hide Player Seekbar:
```java
.setHideSeekBar(true) //default value is false 
```

### Custom TrimTypes

#### TrimType Default:
```java
TrimVideo.activity(videoUri)
          .start(this,startForResult);
```

#### TrimType Fixed Duration:
```java
TrimVideo.activity(videoUri)
          .setTrimType(TrimType.FIXED_DURATION)
          .setFixedDuration(30) //seconds
          .start(this,startForResult);
```

#### TrimType Minimum Duration:
```java
TrimVideo.activity(videoUri)
          .setTrimType(TrimType.MIN_DURATION)
          .setMinDuration(30) //seconds
          .start(this,startForResult);
```

#### TrimType Min-Max Duration:
```java
TrimVideo.activity(videoUri)
          .setTrimType(TrimType.MIN_MAX_DURATION)
          .setMinToMax(10, 30)  //seconds
          .start(this,startForResult);
```

## Proguard Rules
```pro
-dontwarn com.hi.library**
-keep class com.hi.library** { *; }
-keep interface com.hi.library** { *; }
```

## Compatibility
  
  * Library - API 24+
  * Sample - API 24+

## License

This library is licensed under the MIT License.

## LiTr Notice

This project includes **LiTr** (Lightweight Video/Audio Transformer by LinkedIn), licensed under the [BSD‑2‑Clause License](https://opensource.org/licenses/BSD-2-Clause).

### Obtaining FFmpeg Source Code

You can obtain the source code for LiTr from https://github.com/linkedin/LiTr

## Support 
Show your support by giving a star to this repository. 
  
## Collaboration
There are many ways of improving and adding more features, so feel free to collaborate with ideas, issues and/or pull requests. 
