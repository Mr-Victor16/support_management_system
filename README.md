# Support management system
Prosty system zarządzania wsparciem technicznym, stworzony na potrzeby projektu indywidualnego z zajęć Platform Programowania.
Przy tworzeniu projektu użyto: Java Spring, Spring Security, Thymeleaf, Bootstrap, HTML.

## Możliwości systemu
- rejestracja/logowanie do systemu (z wysyłaniem maila aktywacyjnego)
- podział na 3 role: użytkownik, operator wsparcia technicznego, administrator systemu
- dodawanie/edycja/usuwanie wpisów do bazy wiedzy, oprogramowania, kategorii, statusów, zgłoszeń oraz użytkowników
- generowanie pliku PDF ze zgłoszenia
- zmiana statusu/dodawanie odpowiedzi do zgłoszeń - skutkuje to powiadomieniem mailowym do dodającego zgłoszenie
- edycja danych własnego konta
- wyszukiwanie zgłoszeń/użytkowników/wpisów

## Dane do logowania w systemie
- **Użytkownik**
  - Login: user
  - Hasło: user

- **Operator**
  - Login: operator
  - Hasło: operator

- **Administrator**
  - Login: admin
  - Hasło: admin

## Konfiguracja
Aby móc uruchomić aplikację, należy w pliku **application.properties** wprowadzić dane logowania do wysyłania wiadomości e-mail przez system (spring.mail.host, spring.mail.port, spring.mail.username, spring.mail.password).

## Przykład wyglądu
![screenshot](https://user-images.githubusercontent.com/101965882/163582629-19fea2dc-12cf-461e-ad45-1176d25af3d1.png)
Podgląd zgłoszenia z poziomu administratora.
