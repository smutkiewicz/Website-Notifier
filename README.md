 # Page Notifier
 Często czekając na konkretną informację (w moim przypadku były to wyniki kolokwium, czy egzaminu) na konkretnej stronie, sprawdzałem ją całkiem sporo razy na dzień. Proces - odpalanie przeglądarki i szukanie na stronie informacji wydał mi się męczący (gdy robi się to średnio co 10 minut). Zainstalowałem więc wtyczkę do Firefoxa informującą o zmianach na stronie. Działała ona jednak tylko, gdy włączony był Firefox. Potrzebuję więc wygodniejszego sposobu na elastyczne, szybkie i wygodne powiadamianie.
 
 # aplikacja
 Implementacja "sprawdzacza" w systemie Android. Aplikacja, która po wprowadzeniu szczegółów, będzie na bieżąco, w tle, sprawdzać konkretną stronę w poszukiwaniu zmian od chwili wydania polecenia obserwacji.

 # screenshoty (wersja beta 0.8)
 
<div align="center">
   <img src="screenshots/main_ss.png" width="25%" />
   <img src="screenshots/details_ss.png" width="25%" />
   <img src="screenshots/newtask_ss.png" width="25%" />
   <img src="screenshots/settings_ss.png" width="25%" />
</div>

 # czego się nauczyłem:
* Systematycznego, etapowego i rozmyślnego podejścia do projektu - najpierw layout, potem kod UI (szkielet działania), a na koniec implementacja
* Korzystania z ContentProviders (wygodny dostęp do bazy danych z każdego miejsca w projekcie
* Korzystania z serwisów i zasad ich działania (najpierw próbowałem IntentService, potem okazało się, że JobScheduler pasuje lepiej do mojego typu zadania)
* Improvement w projektowaniu architektury - starania w celu trzymania się zasad rozdzielenia abstrakcji (jest sporo do poprawy, ale jest już zdecydowanie lepiej niż kiedyś)
* Pisania lepszego kodu (zasada pojedynczej odpowiedzialności, opisowe nazwy zmiennych i funkcji, usystematyzowanie !jednolitego! stylu !W KOŃCU!)

 # czego muszę się jeszcze nauczyć:
* Testy - mockowanie, Espresso, JUnit, bo wciąż nie ma u mnie kultury testera: "najpierw testy - potem implementacja"
* Wielowątkowość - udało się ją osiągnąć, ale to był ten pierwszy raz i następny musi być lepszy!


 # TODO
 * ~~Zacząć~~ (Done!)
 * ~~Layout aplikacji~~ (Done!)
 * ~~Implementacja logiki UI reagującej na serwis~~ (Done!)
 * ~~Opcja dodawania itemów z adresami stron do bazy i przeglądania ich z możliwością edycji i usuwania~~ (Done!)
 * ~~Opcja uruchomienia usługi porównującej stronę z daną przez użytkownika częstotliwością (działającej w tle niezależnie od aktywności głównej~~ (Done!)
 * ~~Opcja wysłania powiadomienia przez usługę działającą w tle~~ (Done!)
 * ~~Refactor metod do uruchamiania serwisu w MainActivity (może oddzielna klasa?)~~ (Done!)
 * ~~Logo aplikacji i tłumaczenie stringów na język angielski~~ (Done!)
 * ~~Uruchamianie zadania w zależności od dostępności WiFi lub sieci~~ (Done!)
 * Porządne czyszczenie kodu - usunięcie logów do debuggowania
 * Testy testy testy i jeszcze raz testy!
 * Śledzenie pobranej ilości danych


