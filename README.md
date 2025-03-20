# Programmation Fonctionelle - Projet AKKA

Célian Mignot  
Clément Delamotte  
Alexandre Grisez  
Olivier Compagnon--Minarro  
Lyz Piam  

ING2 GSI  

## Prérequis
- ### Scala :

Vous devez avoir coursier d'installé pour installer Scala sur votre machine, si je n'est pas le cas, taper les commandes suivantes dans un terminal :
```bash
curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-linux.gz | gzip -d > cs && chmod +x cs && ./

cs setup
```
Si vous ne disposez pas de ```curl``` sur votre ordinateur :
```bash
sudo apt-get install curl
```

Pour terminer, tapez la commande ci-dessous pour installer scala :
```bash
cs install scala:2.13.15
```
- ### sbt :
```bash
cs install sbt
```
- ### Node.js :

Commencez par taper la commande dans un terminal :
```bash
sudo apt update
```
Ensuite saisir la commande suivante :
```bash
sudo apt install nodejs
```

Vous avez terminé, si vous souhaite vérifier si ```Node.js``` à bien été installé, vous pouvez taper la commande :
```bash
node -v
```
- ### npm : 
Tapez les commandes suivantes :
```bash
sudo apt install npm
npm install recharts
```
- ### MongoBD :  
Depuis un terminal, installez ```gnupg``` et ```curl``` si vous ne les avez pas sur votre machine

Ensuite vous devez importer la clé publique MongoDB avec cette commande :
```bash
curl -fsSL https://www.mongodb.org/static/pgp/server-8.0.asc | \
   sudo gpg -o /usr/share/keyrings/mongodb-server-8.0.gpg \
   --dearmor
```

Ensuite, selon la version de votre système, tapez la commande suivante :
- Ubuntu 24.04 (Noble) :
```bash
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-8.0.gpg ] https://repo.mongodb.org/apt/ubuntu noble/mongodb-org/8.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-8.0.list
```
- Ubuntu 22.04 (Jammy) :
```bash
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-8.0.gpg ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/8.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-8.0.list
```
- Ubuntu 20.04 (Focal) :
```bash
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-8.0.gpg ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/8.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-8.0.list
```

Pour vérifier sous quelle version tourne votre machine, vous pouvez taper la commande :
```bash
cat /etc/lsb-release
```

Continuez en tapant la commande :
```bash
sudo apt-get update
```
Pour terminer, saisir dans un terminal, la commande :
```bash
sudo apt-get install -y mongodb-org
```

## Premier utilisation

Vous devez créer la base de données qui servira pour stocker les utilisateurs et leurs portefeuilles. Pour cela dans un terminal, tapez :
```bash
mongosh
```
Une fois dans l'interface créée par mongosh, tapez la commande suivante :

```bash
use AkkaData
```

Vous avez terminé la création de la base de données, vous pouvez quitter l'interface avec la commande :
```bash
exit
```

## Démarrage de l'application
### Serveur Backend
Ouvrir un terminal (Raccourci clavier : ```Ctrl``` + ```Alt``` + ```T```)

Pour démarrer le serveur Akka, dans la racine du projet, taper la commande :
```bash
sbt run
```
>[!CAUTION]
>Le serveur Akka n'est fonctionnelle que durant 15 minutes. Il faut le redémarrer pour accéder de nouveau à toutes les fonctionnalités de l'application.


### Serveur Frontend
Dans un nouveau terminal, rendez vous dans le dossier frontend :

```bash
cd frontend
```

Tapez la commande suivante pour démarrer le serveur frontend :
```bash
npm start
```

Normalement, une page navigateur va automatiquement s'ouvrir sur la page d'accueil. Si ce n'est pas le cas, dans un navigateur internet, tapez le lien url: http://localhost:3000/

Vous pouvez maintenant profiter de l'application
