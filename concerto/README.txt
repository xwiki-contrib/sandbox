
       -- class header / Copyright (C) 2008  100 % INRIA / LGPL v2.1 --

 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (C) 2008  100 % INRIA
 Authors :
                      
                      Gerome Canals
                    Nabil Hachicha
                    Gerald Hoster
                    Florent Jouille
                    Julien Maire
                    Pascal Molli

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

 INRIA disclaims all copyright interest in the application XWoot written
 by :    
         
         Gerome Canals
        Nabil Hachicha
        Gerald Hoster
        Florent Jouille
        Julien Maire
        Pascal Molli

 contact : maire@loria.fr
 ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 
Developper have to change properties in the default profile in main pom.

Use installer :

- mvn install in the xwootInstaller directory
- run java -jar target/org*jar in the xwootInstaller directory
- it's a simple installer, just click next
- have some shortcuts in the menu
 
To skip tests : 
-Dmaven.test.skip=true (command line)

Please deploy the jlibdiff and openchord jar in to your local repository :
(base dir):
mvn install:install-file -Dfile=./xwootApp/src/main/resources/jlibdiff.jar -DgroupId=fr.loria.ecoo -DartifactId=JLibDiff -Dversion=2.0 -Dpackaging=jar
mvn install:install-file -Dfile=./LTR/config/openchord_1.0.5.jar -DgroupId=de.uniba.wiai.lspi -DartifactId=openchord -Dversion=1.0.5 -Dpackaging=jar

Site deployment :
mvn site:site
mvn site:deploy

Commit sources (not sure) :
mvn -Dmessage="commit message" scm:checkin

Source header update :

Source format :

Generate war and jars :





