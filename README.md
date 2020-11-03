# Generic Springboot Template Application
This application was based on a Springboot Udemy class. It includes all necessary code for a production-style web service, including user authentication/authorization and may serve as a template for future such projects.

## Running on Local
Make sure Java is installed
https://www.appsdeveloperblog.com/how-to-set-java_home-on-mac/

Run ```bash java -version``` to make sure install was successful

Make sure Maven is installed
- https://www.journaldev.com/2348/install-maven-mac-os
- Run ```bash source ~/.profile``` or ```bash source ~/.bash_profile``` to make sure changes to either file take effect immediately.
- Run ```bash mvn -version``` to make sure install was successful

Install Spring Tool Suite (STS)
https://spring.io/tools

Install MySQL
https://dev.mysql.com/downloads/mysql/

Open MySQL Workbench and create new schema named after what ever is specified in the ```bash application.properties``` file
```

Create new MySQL user, getting username and password from application.properties file
```bash Create user '<username>'@'localhost' identified by '<password>';
Grant all privileges on rvb.* to 'budlinville'@'localhost';
Flush privileges;
```
## Add properties file
Add copy of application.properties file to ```bash src/main/resources```

## Add AWS credentials file to system
Save to:
- Linux, Unix, MacOS
```bash ~/.aws/credentials
```
- Windows
```bash C:\Users\<USERNAME>\.aws\credentials
```

### Create STS build configuration
Name: mobile-app-ws
Project: mobile-app-ws
Main type: com.walmart.app.ws.MobileAppWsApplication


## Running on AWS
Fire up an ec2 instance

### SSH into ec2 instance
- Download pem file to local machine
- Change permissions of pem file to read/write ```bash
sudo chmod 600 /path/to/my/key.pem
```
- Update ec2 instance's security group to allow inbound ssh (Port 22) traffic from my local machine's public IP address
- Navigate to directory holding pem file and run: ```bash
ssh -i <keyName.pem> <user>@<publicDNS>

or

ssh -i key.pem ec2-user@ec2-3-21-104-167.us-east-2.compute.amazonaws.com
```

Once inside ec2 instance, update software: ```bash sudo yum update```

Update Java:```bash
java -version
yum list java*
sudo yum install java-1.8.0
sudo /usr/sbin/alternatives --config java
sudo /usr/sbin/alternatives --config javac
java -version
```

Download Tomcat: ```bash
yum list tomcat*
sudo yum install tomcat8
yum list tomcat*
sudo yum install tomcat8-admin-webapps
```

Start tomcat: ```bash sudo service tomcat8 start```
Stop tomcat: ```bash sudo service tomcat8 stop```
Restart tomcat: ```bash sudo service tomcat8 restart```
Check tomcat status: ```bash sudo service tomcat8 status```

### Configure tomcat to access Manager GUI: ```bash
whereis tomcat8
cd /usr/share/tomcat8/conf
sudo vi tomcat-users.xml
```

Add these tags: ```html
<role rolename="manager-gui"/>
<role rolename="admin-gui"/>
<user username="manager" password="manager-password" roles="manager-gui, admin-gui"/>
```

### Connect to Tomcat's Manager GUI from local machine: ```bash
cd ./var/lib/tomcat8/webapps/manager/META-INF
sudo vi context.xml
```
In the "allow" property of the "Valve" tag, add ```bash |<ip.address>```

Can now add/remove/update services at http://<publicDNS>:8080/manager/html

### Install MySQL ```bash
sudo yum install mysql-server
sudo /usr/bin/mysql_secure_installation
```
(The second line is a script for easily configuring your mysql setup)

Start: ```bash sudo service mysqld start```
Login ```bash mysql -u [root | <username>] -p```

Go through instructions listed in "Running on Local" section to create a non-root user

### Connect to remote mysql instance from MySQL Workbench on local machine
- On the ec2 instance you are running MySQL from, update security group to allow inbound traffic via port 3306 from my local machine's public IP address
- Add new MySQL user to be accessed via your local IP ```sql
create user 'remote-user'@'<your.local.ip>' identified by '<password>';
grant all privileges on <db-name>.* to 'remote-user'@'your.local.ip';
flush privileges;
```
