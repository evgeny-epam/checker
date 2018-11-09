"Checker" checks Web sites for broken links. Link verification is done on "normal" links, images, backgrounds, style sheets, scripts and java applets (you can set the preferences what you want to check). 
The idea is pretty simple: Checker goes to the URL you provide as a "base URL" and it finds all links (images, backgrounds, style sheets, etc.) available on that page and stores it in the list, then It goes through this list and checks opened pages for available links on them (if the link directs to the domain which differs from the domain of the "base URL" we do not collect links available there, in order to exclude cases when we validate, for instance, Youtube site). 
The depth of the checking process is also configurable (by default it is set for 3). If in response we receive the code different from 200 we assume that it is potentially broken link and put it in the list to report on it. 
It is fast because it uses a technique known as multithreading. It means that the link checking software retrieves several web pages at the same time (by default 5).

PS: Jsoup library can be used instead of custom one which uses HttpURLConnection 
