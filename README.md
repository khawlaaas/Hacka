# Projet Hacka - Spring Boot

Base du projet validée. Tout le monde doit utiliser Java 17, Spring Boot 3.2.4 et MySQL via WampServer (port 3306).

---

## Configuration Locale

- **URL BDD** : `jdbc:mysql://localhost:3306/hackradar`
- **Port Application** : `8089`

---

## Instructions pour l'Équipe

### 1. Récupérer la dernière version de main
```bash
git checkout main
git pull origin main
```

### 2. Créer sa propre branche de travail
Ne jamais travailler directement sur `main`. Créer une branche par fonctionnalité :
```bash
git checkout -b feature/nom-de-votre-tache
```

### 3. Développer et commiter ses modifications
```bash
git add .
git commit -m "feat: description courte de votre travail"
```

### 4. Envoyer sa branche sur GitHub
```bash
git push -u origin feature/nom-de-votre-tache
```

### 5. Fusionner avec main (Merge)
Aller sur GitHub et ouvrir une **Pull Request (PR)** de votre branche vers `main`.

---

## Règles de Code

- **Package principal** : `com.hacka.Hacka`
- **Gestion des erreurs métier** : utiliser la classe `BusinessException` dans `com.hacka.Hacka.exception`.
