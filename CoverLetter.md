## Tell us what your idea is

 With phones like Pixels constantly pushing the limits of mobile camera hardware and software, recent years have seen a significant push towards mobile photography. As per [statista](https://www.statista.com/chart/10913/number-of-photos-taken-worldwide/), more than **1.2 trillion digital images** were captured in 2018, out of which, **over 85% were taken on a smartphone**. And while services like Google Photos can help people solve the storage problem, there is room for improvement when it comes to managing the clutter in each and every phone&#39;s gallery.

AfterShoot is a camera app that aims to be a second pair of eyes that analyses all the pictures in real-time as you take them.

It filters out the potentially bad shots and recommends improvements that can be made to them.  
The recommendation engine has been trained on pictures captured by a variety of sources ranging from professionals to hobbyist photographers and various photography techniques like **Rule of Thirds, Rule of Depth** and the **Golden Ratio** to identify an aesthetically pleasing image from a bad one.

Aftershoot can also scan the photos in your gallery to help clear the clutter and help you manage your storage efficiently.

Google&#39;s services I plan on using :

1. AutoML for training models to detect bad picture
2. Tensorflow to train the model to take parameters for a bad picture and suggest potential fixes
3. Firebase MLKit to serve these models and run them on the device

## Tell us how you plan on bringing it to life

I&#39;m currently working on the models to identify and filter out good shots from the bad ones and so far I have:

1. Built the model that identifies blurry, overexposed, underexposed and best shot from duplicate shots : **[models/README.md](models/README.md)**
2. Made a skeleton camera app (built upon the open-source AOSP camera) which will be integrated with the models above : **[camera/README.md](camera/README.md)**

Google has already worked on identifying good shots from bad shots with Google Clips and I believe that the learning that Google had from Google Clips can help me accelerate the development of this project.

My timeline for the project is :

- **November - December 2019:** Complete working on the models that identify potentially bad pictures :
  - [x] Blurry/Out of Focus - **Done** 
  - [x] Blinks - **Done**
  - [x] Over/Underexposed - **Done**
  - [ ] Best image from Duplicates, Sad Faces in an image, Cropped human faces, Foreign Objects in the picture - **Work in Progress**
- **January - February 2019:** Work on a model that will take the flaw in the image (determined from the models above) and apply or suggest a fix that can be made to improve that image.
- **March 2019:** Add finishing touches to the camera app and integrate it with the ML models
- **April 2019:** Finish working on the app module that will scan the user&#39;s gallery for bad images.
- **May 2019:** Beta test of the first version of the app

Most of the source code I currently have is for training the models above, so I won&#39;t be able to share it at the moment. Source code for the camera, however, will be made public before the beta release of the app.

## Tell us about you

Android Developer and an avid tech blogger, I&#39;m passionate about anything and everything related to Android. Currently a Google Developer Expert for Firebase; I am also one of the first Google-certified Android developers based out of India. Being an Open Source enthusiast, I&#39;ve been a regular part of programs like Google Summer of Code and Google Code-In, both as a Mentor and a student.

Other than Google, I&#39;ve been a regular contributor to Open Source Organizations like Apache, Redhat, and FOSSASIA. Recently I have been inclined towards my new found interest that combines my knowledge of Mobile Development with Machine Learning to create smart mobile applications.

Android Projects that I have worked on in the past:

1. [Offix-Android:](https://github.com/aerogear/offix-android) Worked as a maintainer of this open-source SDK that provided offline support and Conflict Resolution for people using GraphQL in their Android Apps.
2. [Open Event Android:](https://github.com/fossasia/open-event-droidgen) Worked on an Android App Generator project that helped Event Organizers to generate Android and Web apps for their event with a single click
3. [Pokidex:](https://play.google.com/store/apps/details?id=app.harshit.pokedex&amp;hl=en_IN) Android app that identifies Pokemon from the captured image, a real-life Pokedex
4. [CrapCons:](https://play.google.com/store/apps/details?id=com.dagger.crapcons) An icon pack with a twist! CrapCons was the first icon pack for Android with all its assets made entirely in MS Paint.
5. [ZooperCrap](https://play.google.com/store/apps/details?id=com.adam.zwskin.zoopercrap&amp;hl=en_IN): Widget extension for CrapCons
6. [Sixgrid](https://play.google.com/store/apps/details?id=io.pure.sixgrid): A minimal and battery-friendly launcher for Android
