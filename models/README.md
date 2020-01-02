We're using Google Cloud AutoML Vision Edge to train Machine Learning models to identify potentially bad pictures and so far we've managed to identify Blurred, Over Exposed, Under Exposed and Blinks in a picture.  
These models are tensorflow_lite models that run directly on device without requiring an internect connection.  

Next steps are to train ML models to identify more potential flaws in an image.

### Blurred  
TfLite model that can detect and identify blurred pictures from a non-blurred or a background blurred image
![](https://i.imgur.com/KBnHicS.png)  

### Exposure  
TfLite model that can detect images with poor lighting 
![](https://i.imgur.com/ARpRUO9.png)  

### Blinks  
We can use Firebase MLKit's Face detection API to determine if the subject's eyes are closed alongwith their facial expressions (like Smiling probability) to determine if the blink was intentional or unintentional.  
I've described how this can be done here: https://heartbeat.fritz.ai/blink-detection-on-android-using-firebase-ml-kits-face-detection-api-6d09823db535  
![](https://i.imgur.com/0H5zngK.png)

### Cropped faces
TfLite model that can detect images containing cropped faces. 
Another alternate solution is using Pose estimation models to detect whether the subject in question has certain keypoints visisble.  

We're working on both of these solutions and will go ahead with the one that works the best.

![](https://i.imgur.com/WX66tTd.png)  
![](https://i.imgur.com/u4ZWFE7.png)
