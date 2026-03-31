# Git & GitHub Setup Instructions

Follow these steps to set up version control for the Block Crusher game.

## 1. Install Git (if not already installed)

### Windows
- Download Git from [git-scm.com](https://git-scm.com/download/win)
- Run the installer with default options (ensure "Git from the command line and also from 3rd-party software" is selected).
- After installation, open a new **Command Prompt** or **PowerShell** and verify:
  ```cmd
  git --version
  ```

### macOS
- Install via Homebrew:
  ```bash
  brew install git
  ```
- Or download from [git-scm.com](https://git-scm.com/download/mac)

### Linux
- Use your package manager:
  ```bash
  sudo apt install git          # Debian/Ubuntu
  sudo yum install git          # RHEL/CentOS
  sudo pacman -S git            # Arch
  ```

## 2. Configure Git (if first time)

Set your name and email (required for commits):
```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

## 3. Initialize Local Repository

Navigate to the project folder (where this file is located) and run:
```bash
git init
```

This creates a `.git` directory.

## 4. Add Remote Repository on GitHub

### Create a New Repository on GitHub
1. Go to [GitHub](https://github.com) and sign in.
2. Click the **+** icon in the top right and select **New repository**.
3. Enter repository name (e.g., `block-crusher-game`).
4. Choose **Public** or **Private**.
5. **Do NOT** initialize with README, .gitignore, or license (we already have them).
6. Click **Create repository**.

### Link Local Repository to Remote
Copy the remote URL (HTTPS or SSH) from the newly created repository page.

In your terminal, run:
```bash
git remote add origin <your-repo-url>
```
Example (HTTPS):
```bash
git remote add origin https://github.com/your-username/block-crusher-game.git
```

## 5. Stage and Commit Initial Files

Add all project files to the staging area:
```bash
git add .
```

Commit with a descriptive message:
```bash
git commit -m "Initial commit: project structure and basic docs"
```

## 6. Push to GitHub

Push the commit to the remote repository:
```bash
git branch -M main
git push -u origin main
```

If you encounter authentication issues, you may need to set up a Personal Access Token (PAT) or SSH keys. See [GitHub authentication docs](https://docs.github.com/en/authentication).

## 7. Verify

Visit your GitHub repository page; you should see the committed files.

## Troubleshooting

### Git not found
- Ensure Git is installed and added to PATH.
- Restart your terminal after installation.

### Remote origin already exists
If you need to change the remote URL:
```bash
git remote set-url origin <new-url>
```

### Authentication failed
- Use a Personal Access Token instead of a password (required since 2021).
- Generate a token: [GitHub Settings → Developer settings → Personal access tokens](https://github.com/settings/tokens)
- Use the token as your password when prompted.

### Push rejected
If the remote repository already has commits (unlikely), you may need to pull first:
```bash
git pull origin main --allow-unrelated-histories
```

---

Once the repository is set up, you can proceed with game development. The project is now ready for PHASE 1.