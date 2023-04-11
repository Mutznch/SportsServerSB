# SportsServerSB
Sport Server in Spring Boot

Nome do projeto: Match

Integrantes: Félix Augustus Motelevicz

Link do vídeo: "https://www.youtube.com/watch?v=qnb2MDRCEQo"

Como executar:

  Para executar o programa, crie um novo projeto java utilizando o IntelliJ e jogue os
  arquivos e pastas deste repositório na pasta do projeto recem criado.
  
  Carregue as dependencias do projeto Maven "pom.xml".
  
  Na pasta src/main/resources/ abra o "application.properties" e altere o usuario e senha (linhas 5 e 6)
  para os dados do usuario do banco de dados que será utilizado. Banco de dados utilizado: MySQL
  
  Crie um schema no banco de dados com o nome "sportsappdb" ou altere a linha 4 do application.properties
  para a rota do schema que será utilizado.
  
  Para rodar o projeto, na pasta src/main/java/br/pucpr/sportsserver execute a função main da classe "SportserverApplication.java"
  
  Para acessar o swagger com as rotas do servidor, acesse o link "http://localhost:8080/sportserver/api/"
