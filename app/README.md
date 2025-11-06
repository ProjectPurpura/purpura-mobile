# 📱 Purpura App

![Java](https://img.shields.io/badge/Language-Java-orange?style=for-the-badge&logo=android)
![Android](https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

Um aplicativo de e-commerce moderno desenvolvido em Java nativo para Android, focado em facilitar a compra e venda de produtos com integração de pagamentos PIX e QR Code. O aplicativo oferece uma experiência de usuário fluida com navegação intuitiva e design responsivo.

## ✨ Principais Funcionalidades

O aplicativo foi desenvolvido com um fluxo completo de e-commerce, desde o cadastro até o pagamento.

#### 🚀 Tela de Splash (`SplashScreen`) - *Ponto de Entrada*
* **🎨 Apresentação da Marca:** Tela de boas-vindas com logo e nome da empresa em fundo roxo personalizado.
* **⏱️ Transição Automática:** Após 4 segundos, redireciona automaticamente para a tela de registro/login.
* **🎯 UX Otimizada:** Interface limpa e profissional para criar uma primeira impressão positiva.

#### 🔐 Sistema de Autenticação
* **📝 Registro Completo:** Fluxo de cadastro com múltiplas etapas incluindo dados pessoais, endereço e chave PIX.
* **🔑 Login Seguro:** Autenticação com opções de login tradicional e integração com Google.
* **📍 Cadastro de Endereço:** Formulário dedicado para coleta de informações de localização.
* **💳 Configuração PIX:** Tela específica para cadastro de chaves PIX para pagamentos.

#### 🛍️ E-commerce e Produtos
* **📱 Página de Produto:** Interface detalhada com informações completas do produto incluindo:
    * Nome, preço e descrição do produto
    * Dados da empresa vendedora
    * Peso e localização do produto
    * Botões para adicionar ao carrinho e conversar com vendedor
* **🛒 Carrinho de Compras:** Funcionalidade para gerenciar produtos selecionados.
* **💬 Chat com Vendedor:** Comunicação direta entre comprador e vendedor.

#### 💰 Sistema de Pagamento
* **💳 Métodos de Pagamento:** Tela para seleção entre diferentes formas de pagamento.
* **📱 Pagamento PIX:** Integração com sistema PIX para transferências instantâneas.
* **📊 QR Code:** Geração e leitura de QR codes para pagamentos rápidos.
* **✅ Status de Pagamento:** Acompanhamento do status das transações (processando, aprovado, falhou).

#### 🏠 Dashboard Principal
* **📊 Navegação por Abas:** Interface com navegação inferior entre Home, Dashboard e Conta.
* **🏠 Home:** Tela principal com funcionalidades essenciais.
* **📈 Dashboard:** Área de controle e estatísticas.
* **👤 Conta:** Gerenciamento de perfil e configurações do usuário.

#### 🚨 Tratamento de Erros
* **🌐 Erro de Internet:** Tela dedicada para problemas de conectividade.
* **⚠️ Erro Genérico:** Tratamento de erros inesperados com interface amigável.

## 🛠️ Arquitetura e Tecnologias Utilizadas

A arquitetura do app foi estruturada seguindo as melhores práticas do Android, com separação clara de responsabilidades e componentes reutilizáveis.

* **Linguagem:** **Java**
* **Framework:** **Android SDK Nativo**
* **Bibliotecas Principais:**
    * **AndroidX Libraries:** `AppCompat`, `ConstraintLayout`, `CardView`, `RecyclerView`
    * **Material Components:** Para componentes modernos e consistentes
    * **Navigation Component:** Para navegação entre telas e fragments
    * **ViewBinding:** Para binding seguro de views
    * **Lifecycle Components:** `ViewModel`, `LiveData` para gerenciamento de estado
    * **Google Play Services:** Integração com serviços do Google

#### 📁 Estrutura do Projeto
```
app/
├── src/main/java/com/purpura/app/
│   ├── configuration/          # Classes utilitárias
│   ├── ui/
│   │   ├── screens/            # Activities principais
│   │   ├── home/              # Fragment Home
│   │   ├── dashboard/         # Fragment Dashboard  
│   │   └── account/           # Fragment Account
│   └── res/                   # Recursos (layouts, drawables, etc.)
```

## 🚀 Como Executar o Projeto

1. **Pré-requisitos:**
   * Android Studio (versão mais recente)
   * Android SDK (API 24+)
   * Git

2. **Clonagem:**
   ```bash
   git clone https://github.com/seu-usuario/purpura-app.git
   cd purpura-app
   ```

3. **Build e Execução:**
   * Abra o projeto no Android Studio
   * Aguarde a sincronização do Gradle
   * Execute o projeto em um emulador ou dispositivo físico

## 📱 Compatibilidade

* **Android:** API 24+ (Android 7.0)
* **Orientação:** Retrato (otimizado)
* **Tema:** Modo claro com cores personalizadas

## 🎨 Design System

O aplicativo utiliza uma paleta de cores consistente:
* **Roxo Principal:** `#724B9D` / `#704999`
* **Roxo Claro:** `#E4CFFF`
* **Elementos:** Bordas arredondadas, sombras suaves e transições fluidas

## 👨‍💻 Autores

* **[Dora3003](https://github.com/Dora3003)**
* **[JhowzinSql](https://github.com/JhowzinSql)**

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
