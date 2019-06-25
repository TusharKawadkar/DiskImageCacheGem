# Disk Image Cache gem - Make image loading/reloading from network in fastest way.

[![Release](https://jitpack.io/v/TusharKawadkar/DiskImageCacheGem.svg)](https://jitpack.io/#TusharKawadkar/DiskImageCacheGem)

DiskImageCacheGem is an android library to cache the images downloaded from network in disk as well as in cache memory. It works the same way as the OS access data from disk and RAM.

The library loads the image from URL and put it into cache as well as in disk (database). Once you close the app and clear it from memory and open the app again, it loads the data from disk to cache memory. Now you already have all the bitmaps so loading an image from URL becomes fast.

It also maintains a sync between cache and disk. With the usage of few functions, you will be able to load and set images.


## Installation

### Gradle

Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

```bash
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Step 2. Add the dependency

```bash
dependencies {
	        implementation 'com.github.TusharKawadkar:DiskImageCacheGem:0.2'
	}
```


## Usage

Initialize in application's onCreate() method. This will initialize and prefetch the images from disk to cache on app startup.

```bash

DiskImageCacheGem.Companion.getInstance().preFetchImages(this);
```

This is will load the image and cache it into memory and disk. If the image already exists in the cache then it will load the bitmap into image view.

```bash
DiskImageCacheGem.getInstance().setImage(context, url, imageView)
```


## License

MIT License

Copyright (c) 2019 Tushar Kawadkar

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
