# 📘 Présentation du Projet : Serveur Multithreadé en Java 

## 🧠 Contexte

Ce logiciel est un client de chat modulaire conçu pour offrir une expérience conversationnelle , autonome et extensible.  
Il intègre une interface simple de communication , une gestion des groupes par commandes, et une interface néon arrondie pensée pour la clarté émotionnelle.

> ![](../Pictures/00.png)

---

## 🚀 Fonctionnalités Principales

- **Gestion des groupes par commandes textuelles**
  - `/group create nom_du_groupe`
  - `/group join nom_du_groupe`
  - `/group leave nom_du_groupe`
- **Assistant conversationnel avec mémoire contextuelle**
- **Interface néon, arrondie, avec feedback en temps réel**
- **Parsing robuste et validation des commandes**
- **Architecture modulaire pour extensions techniques**

> ![](../Pictures/01.png)

---

## ⚙️ Fonctionnement

### 🔹 Lancement

L'utilisateur ouvre le client et interagit directement avec l'assistant.  
Aucun bouton GUI n'est nécessaire : tout se fait par commandes ou dialogue naturel.

### 🔹 Interaction

Les commandes sont analysées et exécutées avec retour immédiat.  
L'assistant guide l'utilisateur en cas d'erreur ou d'ambiguïté.

###   Gestion des Utilisateurs et des déconnexions

![](../Pictures/002.png)

### 🔹 Feedback

Chaque action déclenche un retour visuel : animations, messages, ou transitions.  
Le panneau d’aide s’adapte dynamiquement selon le rôle et le contexte.

### 🔹 Extension

De nouveaux modules peuvent être ajoutés pour :
- Etendre la communication sur tout type de support
- Automatiser des tâches techniques
- Intégrer des modèles ML
- Enrichir les interactions avec des styles d'émojis etc.........


---

## 🎨 Design & Expérience Utilisateur

- Couleurs néon : bleu électrique, violet, vert acide
- Bords arrondis, ombrages doux, transitions fluides
- Interface intuitive, émotionnellement guidée
- Équilibre entre puissance technique et clarté visuelle


## 🛠️ Notes Techniques

- Parsing des commandes basé sur découpage explicite
- Gestion des erreurs avec messages guidés
- Architecture pensée pour multi-threading et extensibilité

---
Demo et code disponible à l'adresse 
	
https://github.com/Artemisxxx37/multi_threaded_java_server.git