package com.qc;

import org.apache.commons.cli.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class WebShot {

    // En fonction du système d'exploitation, retourne le nom du dossier contenant le driver adapté
    private static String getSpecificDriverPath() {
        return (System.getProperty("os.name").toLowerCase().contains("mac")) ? ("macos") : ("linux");
    }

    // Configure les options d'aide du programme
    private static Options configHelpOptions() {
        // Ajouter une option pour afficher l'aide de l'application.
        // Commandes: "--help", "-h"
        final Option helpFileOption = Option.builder("h")
                .longOpt("help")
                .desc("Affiche le message d'aide")
                .build();

        final Options helpOptions = new Options();

        helpOptions.addOption(helpFileOption);

        return helpOptions;
    }

    // Configure les paramètres du programme
    private static Options configParameters(final Options helpOptions) {
        // Ajouter une option pour spécifier l'adresse URL du site à capturer
        // Commandes: "--url", "-u"
        final Option urlOption = Option.builder("u")
                .longOpt("url")
                .desc("Adresse URL du site")
                .hasArg(true)
                .argName("url")
                .type(String.class)
                .required(true)
                .build();

        // Ajouter une option pour spécifier le navigateur à utiliser
        // Commandes: "--driver", "-d"
        final Option driverOption = Option.builder("d")
                .longOpt("driver")
                .desc("Navigateur à utiliser (chrome, firefox)")
                .hasArg(true)
                .argName("driver")
                .type(String.class)
                .required(true)
                .build();

        // Ajouter une option pour spécifier le nom du fichier à générer
        // Commandes: "--filename", "-f"
        final Option filenameOption = Option.builder("f")
                .longOpt("filename")
                .desc("Nom du fichier à générer")
                .hasArg(true)
                .argName("filename")
                .type(String.class)
                .required(false)
                .build();

        // Ajouter une option pour spécifier la largeur souhaitée pour la capture
        // Commandes: "--width", "-w"
        final Option widthOption = Option.builder("w")
                .longOpt("width")
                .desc("Largeur en pixels de la capture")
                .hasArg(true)
                .argName("width")
                .type(Integer.class)
                .required(false)
                .build();

        // Ajouter une option pour spécifier si headless ou pas
        // Commandes: "--headless", "-h"
        final Option headlessOption = Option.builder("h")
                .longOpt("headless")
                .desc("Lancer le navigateur en mode headless")
                .hasArg(false)
                .required(false)
                .build();

        final Options options = new Options();

        // Ajouter d'abord la commande permettant d'afficher l'aide
        for (final Option fo : helpOptions.getOptions()) {
            options.addOption(fo);
        }

        // Ajouter ensuite les autres options
        options.addOption(urlOption);
        options.addOption(driverOption);
        options.addOption(filenameOption);
        options.addOption(widthOption);
        options.addOption(headlessOption);

        return options;
    }

    // L'extension du fichier à enrégister
    private static String fileExtension = "JPEG";

    // Récuperer le chemin de l'exécutable
    private static String projectPath = System.getProperty("user.dir");

    // Clés de variable d'environnement pour les navigateurs supportés
    private static String CHROME_DRIVER_KEY = "webdriver.chrome.driver";
    private static String FIREFOX_DRIVER_KEY = "webdriver.gecko.driver";

    // Chemin vers les exécutables des navigateurs supportés
    private static Map<String, String> driversEnvPath = new HashMap<String, String>() {{
        put("webdriver.gecko.driver", projectPath + "/drivers/" + getSpecificDriverPath() + "/geckodriver");
        put("webdriver.chrome.driver", projectPath + "/drivers/" + getSpecificDriverPath() + "/chromedriver");
    }};
    private static Map<String, String> driversEnvKey = new HashMap<String, String>() {{
        put("chrome", CHROME_DRIVER_KEY);
        put("firefox", FIREFOX_DRIVER_KEY);
        put("gecko", FIREFOX_DRIVER_KEY);
    }};

    // Générer un nom de fichier
    private static String generateFileName(String prefix) {
        return prefix + "@" + System.currentTimeMillis() / 1000L + "." + fileExtension;
    }

    // Retourner le chemin complet vers le fichier à enrégistrer
    private static String getFilePath(String fileName) {
        return projectPath + "/dist/" + fileName;
    }

    // Afficher le message d'aide du programme
    private static void showHelp(String prefix, Options options) {
        // Afficher un message s'il manque une option requise
        if (prefix.trim().length() > 0) System.err.println(prefix + "\n");

        if (options != null) {
            // Afficher l'aide, en se basant sur les options que nous avons déjà configuré
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("WebShot", options, true);

            // Afficher un exemple d'exécution
            System.out.println("\nexample: " +
                    "\n\t--url https://www.seleniumhq.org/ --driver chrome --width 1280 --headless --filename=NomDuFichier" +
                    "\n\t--u=https://www.seleniumhq.org/ -d=chrome -w=1280 -f NomDuFichier --headless" +
                    "\n\t-u https://www.seleniumhq.org/ -d firefox -w 800 -h");
        }

        System.exit(0);
    }

    // Prend en paramètres le navigateur, capture l'écran et retourne le chemin du fichier final
    private static String takeScreenshot(WebDriver driver, String filename) throws IOException {
        String destFilePath = getFilePath(filename + "." + fileExtension);

        if (filename == null || Objects.equals(filename, "")) {
            destFilePath = getFilePath(generateFileName(driver.getTitle()));
        }

        ShootingStrategy shootingStrategyForRetina = ShootingStrategies.viewportRetina(2000, 0, 0, 2);
        ShootingStrategy shootingStrategyForNonRetina = ShootingStrategies.viewportPasting(1000);

        Screenshot screenshot = new AShot()
                .shootingStrategy((getSpecificDriverPath().equals("macos")) ? shootingStrategyForRetina : shootingStrategyForNonRetina)
                .takeScreenshot(driver);
        ImageIO.write(screenshot.getImage(), fileExtension, new File(destFilePath));

        return destFilePath;
    }

    // Yes, on y va
    public static void main(String[] args) throws ParseException {

        // Les options d'aide
        final Options firstOptions = configHelpOptions();

        // Les options nécessaires pour le programme
        final Options options = configParameters(firstOptions);

        // Le parseur des options
        final CommandLineParser parser = new DefaultParser();

        // On commence d'abord par parser les options d'aide (s'il y en a)
        final CommandLine firstLine = parser.parse(firstOptions, args, true);

        // Si l'utilisateur demande l'aide, on l'affiche et on quitte le programme
        boolean helpMode = firstLine.hasOption("help");
        if (helpMode) {
            showHelp("", options);
        }

        try {
            // Sinon, on parse les options et ...
            final CommandLine line = parser.parse(options, args);

            // on récupère les options nécessaires,
            // url, driver, filename, headless, width
            String url = line.getOptionValue("url");
            String useDriver = line.getOptionValue("driver");
            String filename = line.getOptionValue("filename");
            boolean headlessMode = line.hasOption("headless");
            int width = 0;
            try {
                width = Integer.parseInt(line.getOptionValue("width"));
            } catch (NumberFormatException ignored) {
            }

            // Charger les drivers des navigateurs dans les variables d'environnement
            String useDriverKey = driversEnvKey.get(useDriver);
            System.setProperty(useDriverKey, driversEnvPath.get(useDriverKey));

            WebDriver driver = null;
            if (useDriverKey.equals(CHROME_DRIVER_KEY)) {
                ChromeOptions chromeOptions = new ChromeOptions();

                chromeOptions.addArguments("--no-sandbox");
                if (headlessMode) chromeOptions.addArguments("--headless");

                driver = new ChromeDriver(chromeOptions);
            } else if (useDriverKey.equals(FIREFOX_DRIVER_KEY)) {
                FirefoxBinary firefoxBinary = new FirefoxBinary();

                if (headlessMode) firefoxBinary.addCommandLineOptions("--headless");

                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setBinary(firefoxBinary);

                driver = new FirefoxDriver(firefoxOptions);
            }

            if (driver != null) {
                // Maximiser la fenêtre du navigateur
                driver.manage().window().maximize();

                // Si l'utilisateur a renseigné une largeur, on le prend, sinon on prend toute la largeur de la fenêtre par défaut
                int driverWidth = width > 0 ? width : driver.manage().window().getSize().getWidth();
                int driverHeight = driver.manage().window().getSize().getHeight();
                Dimension dimension = new Dimension(driverWidth, driverHeight);
                driver.manage().window().setSize(dimension);
                driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);

                // Accéder à l'URL demandé
                driver.get(url);

                // Prendre la capture et sauvegarder le fichier avec un nom spécifié
                String filePath = takeScreenshot(driver, filename);

                // Ecrire le chemin vers le fichier dans la console
                // Permettra au programme appelant de pouvoir accéder au fichier directement
                // Dans notre cas, l'API pourra mettre le fichier à ce chemin en mode Téléchargement en reponse au client
                System.out.println(filePath);

                // Quitter le navigateur
                driver.quit();
            }
        } catch (MissingOptionException e) {
            // S'il y a eu un problème de parsing ou une option a manqué,
            // on affiche le message d'aide, préfixé du message d'erreur
            showHelp(e.getMessage(), options);
        } catch (IOException e) {
            // Si la capture n'a pas pu être réalisée,
            // on affiche le message d'erreur sans l'aide
            showHelp(e.getMessage(), null);
        }
    }
}
