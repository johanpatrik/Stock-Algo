Deploya BE
0.  kör cd Stock-Algo-BE/
1.  kör cd mvn clean install -DskipTests
2.  kör på din dator terminal: scp -i ~/Desktop/ht-ozbga-digi.pem -P 65529 ~/Desktop/stock-algo/target/websocket-0.0.1-SNAPSHOT.jar ozbga@hitechdynasty.se:.
3.  ssha in på maskinen ssh -i ~/Desktop/ht-ozbga-digi.pem ozbga@hitechdynasty.se -p 65529
4.  Om det redan finns en java applikation deployad så måste dö döda den först genom att:
    3a. kör ps -e ELLER netstat -tunlp | grep java
    3b. hitta den som heter java, ta processen och kör kill -9 [processID]
5.  kör java -jar websocket-0.0.1-SNAPSHOT.jar >> /tmp/server.log & för att deploya
6.  kör disown
7.  kolla loggen tail -f /tmp/server.log

Deploya FE
0. Terminal Servern - 
    a. ssha in till servern ssh -i ~/Desktop/ht-ozbga-digi.pem ozbga@hitechdynasty.se -p 65529
    b. Ifall den gamla versionen inte går bort automatiskt när man deployar den nya, måste detta göras:
        a. sudo rm -rf stock-alert/
        b. sudo rm -rf build/
1. Visual Studio Code
    a. kör "npm run build" i stock-alert
    b. Intellij - gå till build/index.html och Lägg till /monitor innan /static så de blir /monitor/static...
    c. Kopiera först allt till maskinen scp -i ~/Desktop/ht-ozbga-digi.pem -P 65529 -r ~/Desktop/Stock-Algo/stock-alert/build ozbga@hitechdynasty.se:.
2. Terminal Servern
    a. cd /var/www/monitor
    b. sudo rm -rf *
    c. cd
    d. sudo cp -R build/* /var/www/monitor/
    
Deploya BE lokalt
- Deploya databasen
    1. cd docker
    2. docker-compose up -d
    