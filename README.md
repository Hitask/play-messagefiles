## Play message files


This module allows you to split localization `messages` files into separate manageable files. Localized messages prefixed by file name. This allows for granular organization of localizations of large websites with lot of pages.

The module is sucecssfully used at HiTask project management service http://hitask.com for website and web application localization.

You can organize files in folder-per-language structure, containing multiple files:
```
\en
   - homepage.properties
   - products.properties
\fr
   - homepage.properties
   - products.properties
```
localization messages accessed by file. prefix.

### Example:


File `en/homepage.properties` contains:
```
greeting=Welcome!
```

in page template reference this message as 
```
&{'homepage.greeting'}
```


## Configuration

Add following line to `application.conf`:
```
messagefiles.path=/localization
```

Where localization is your chosen path to store the files.


## How it works


* Module loads all messages into global Play Messages context.
* Module searches for messages in a directory defined in application.conf (`messagefiles.path` parameter).
* This directory must contain directories with message files. Name of each directory must be the name of specific locale.
* Module uses `application.langs` parameter in order to load only required locales.
* Each message file must have `.properties` extension.


## Author


Vadim Manikhin
