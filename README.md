# webshot-java
CLI application to take entire website capture. So easy to use.

# Usage
### Show help
```bash
java -jar WebShot.jar -h
```
```bash
usage: WebShot -d <driver> [-f <filename>] [-h] -u <url> [-w <width>]
 -d,--driver <driver>       Navigateur à utiliser (chrome, firefox)
 -f,--filename <filename>   Nom du fichier à générer
 -h,--headless              Lancer le navigateur en mode headless
 -u,--url <url>             Adresse URL du site
 -w,--width <width>         Largeur en pixels de la capture

example:
	--url https://www.seleniumhq.org/ --driver chrome --width 1280 --headless --filename=NomDuFichier
	--u=https://www.seleniumhq.org/ -d=chrome -w=1280 -f NomDuFichier --headless
	-u https://www.seleniumhq.org/ -d firefox -w 800 -h
```

### Command line options
#### --driver, -d : The driver to use
We provide currently 2 drivers, for Firefox and Google Chrome.
Possibles values are: "chrome", "firefox"

#### --filename, -f : The output filename
Save the output filename as the value given. If no value is provided, one will be generated. It will be the current timestamp prefixed by the webpage title and separated with a "@"

#### --headless, -h : Launch browser in headless mode or not
If this option is provided, the browser will be launched in headless mode.
By default, it's value is falsy.

#### --url, -u : The webpage to capture
Specify the webpage URL to capture. Please prefix with "http://"

#### --width, -w : The width to set to the browser window before taking the capture
You can provide a specific width for the browser window. If not, it will be maximized.


### Notes
Two folders are importants to make the application works.

**build/** and **dist/** 

and if you are executing directly the jar file, these folders have to be in same folder with the JAR file.
But if you are executing throught other tool, PHP api as example, so the PHP file have to be in same folder with these folders.

### Example
```bash
java -jar WebShot.jar --url https://www.seleniumhq.org/ --driver chrome --width 1280 --headless --filename=NomDuFichier
```
