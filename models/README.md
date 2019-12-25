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
TfLite model that can detect when the subject can blinked (this is different from the scenario where subject's eyes are closed because or their smile)
![](https://i.imgur.com/AfrORCu.png)

### Cropped faces
TfLite model that can detect images containing cropped faces
![](https://i.imgur.com/Aw8Lb96.png)
