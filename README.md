# NFCReader
A part of Tavis Booth's MA project @ University of Wolverhampton. Written in Java, requires no external libraries.

The code is provided merely to act as a resource for other Java smart card application developers. (in fact, the entire MifareUtils class could be ripped for use in your own project)

You may use the code verbatim or otherwise for any commercial or personal application, as long as credit is given in some way, be it in your source code or in the About section of your project.

If you want to compile the entire application, it is uploaded as an IntelliJ IDEA project - you should just be able to use the checkout from version control feature to load the project into IDEA. Compiling the project to a JAR file has not been tested, but should work in theory.

### Hardware requirements:
* An ACS ACR122U NFC reader. The app is locked specifically to this type of reader, to ensure 100% compatibility with the project. Other readers may work if the softlock is removed, but no guarantees are to be made.
* A Mifare-type card. The app has been tested with Mifare 1K cards and Mifare Ultralight/NXP NTAG213 tags - compatibility with other Mifare products has been built in but not tested.

### Software requirements:
* Java 8 or above.
* The official ACS drivers for the reader must be installed - the stock Windows driver does not work.

### Things to note
* The application uses 4 images, displayed during various states of the program. Please ensure the images directory is placed next to the artifact to allow full functionality. You can customise these images by replacing files in the image directory with your own png files of appropriate resolution to match the dimensions of the target machine's display.

### Known issues
* Placing a card and removing it quickly enough can cause a softlock.

### Bug reports:
If something doesn't work as you expect, please open an issue. I will work to resolve the problem.
