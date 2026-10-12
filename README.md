# Spring Praktika — Bean Scope-ları və Dependency Injection

**Java 17 · Spring Boot 3.5.6 · Gradle · az.iktex.organization.structure.testders**

Bu, orijinal dərs layihəsi verilmədiyi üçün sıfırdan hazırlanmış nümunədir. Altı bölmənin **son işlək vəziyyəti** birləşdirilib. Təlimatda tələb olunan *səhv → nəticə → düzəliş* mərhələlərini real olaraq işə salıb screenshot çəkmək, həmçinin hər bölmədən sonra git commit etmək lazımdır. Hazırkı ZIP əvvəlki commit tarixçəsini əvəz etmir.

## İşə salınması

Java 17 tələb olunur. Gradle 8.x quraşdırılıbsa:

```bash
gradle bootRun
gradle test
```

Gradle Wrapper əlavə olunubsa `./gradlew bootRun` / `./gradlew test` istifadə edin.

API: `http://localhost:8080`

```bash
curl http://localhost:8080/api/v1/user-info/all
curl http://localhost:8080/api/v1/user-info/1
curl -X POST http://localhost:8080/api/v1/user-info/create -H 'Content-Type: application/json' -d '{"name":"Aysel","email":"aysel@example.com"}'
curl http://localhost:8080/api/v1/user-info/recent
curl http://localhost:8080/api/v1/scope-demo
curl http://localhost:8080/api/v1/stats
```

**Session testi:** eyni cookie faylından istifadə edin:

```bash
curl -c cookies.txt -b cookies.txt http://localhost:8080/api/v1/user-info/1
curl -c cookies.txt -b cookies.txt http://localhost:8080/api/v1/user-info/3
curl -c cookies.txt -b cookies.txt http://localhost:8080/api/v1/user-info/2
curl -c cookies.txt -b cookies.txt http://localhost:8080/api/v1/user-info/1
curl -b cookies.txt http://localhost:8080/api/v1/user-info/recent
curl http://localhost:8080/api/v1/user-info/recent  # başqa sessiya
```

## Bölmə 1 — Singleton

### Tapşırıq 1–2
`InMemoryUserRepository` daxilində `ConcurrentHashMap<Long, UserInfoResponse>` və `AtomicLong.incrementAndGet()` işlədilir. İlk ID = 1.

### Tapşırıq 3–4
`@PostConstruct` üç istifadəçi əlavə edir; konsolda `UserRepository hazirdir, 3 user yuklendi` çıxır. `@PreDestroy` storage-u təmizləyir və log yazır.

### Tapşırıq 5
`getUserById` olmayan ID üçün `NoSuchElementException` atır. `ApiErrorHandler` bunu HTTP 404-ə çevirir.

### Tapşırıq 6
Controller, service və memory repository singleton bean-lərdir. **Normal halda hər konstruktor log-u 1 dəfə** görünür; 10 HTTP sorğusu 10 yeni singleton yaratmır. Log-dakı `hashCode()` bir instance-ı izləmək üçün istifadə edilir. *Mock seçilərsə repository olaraq onun konstruktoru işləyir.*

### Tapşırıq 7
Başlanğıcda `/all` **3 user** qaytarır. Bir POST-dan sonra yeni ID **4** olur; `/4` həmin user-i qaytarır; `/all` **4 user** qaytarır. POST və GET testləri icra olunub; sübut şəkilləri `screenshots/` qovluğunda yerləşdirilir.

### Tapşırıq 8
Adi `HashMap` eyni vaxtda dəyişən çoxsaylı thread-lər üçün təhlükəsiz deyil; məlumat itkisi/race condition yarana bilər. Adi `long`-a `counter++` atomik deyil; paralel sorğular eyni ID ala bilər. Singleton bean-ə eyni anda **bir neçə thread** müraciət edə bilər, konkret sayı server konfiqurasiyası və yükdən asılıdır. `ConcurrentHashMap` və `AtomicLong` buna görə lazımdır.

## Bölmə 2 — Prototype

### Tapşırıq 9–10
`UserAuditLog`: `@Component @Scope("prototype")`, hər bean üçün yeni UUID, tarix və dəyişən action. Konstruktor log-u var.

### Tapşırıq 11 (müvəqqəti səhv variant)
Müvəqqəti olaraq `UserInfoService` konstruktoruna `UserAuditLog auditLog` əlavə edib onu `final` sahədə saxlayın; `createUser` daxilində `auditLog.getAuditId()` çap edin. Eyni singleton service-ə verilən prototype instansiyası yenidən avtomatik yaradılmır. **Gözlənilən: 3 POST üçün 1 fərqli auditId. Bu səhv variant praktik olaraq sınaqdan keçirilməyib.**

### Tapşırıq 12
Son işlək kod `ObjectProvider<UserAuditLog>.getObject()` çağırır. **3 POST üçün 3 fərqli auditId**, `action=CREATE_USER`. Konsol screenshot-u əlavə edin.

### Tapşırıq 13
`@PreDestroy` prototype bean üçün adi tətbiq bağlanışında **avtomatik çağırılmır**. Spring instansiyanı yaradır/inject edir, lakin prototype bean-in destruction lifecycle-ını izləmir. Manual cleanup istisnadır.

## Bölmə 3 — Request

### Tapşırıq 14–17
`RequestContext`: request-scoped `requestId` (UUID-nin ilk 8 simvolu), `startTime` millisaniyə. Controller/service/repository eyni HTTP sorğusunda eyni ID çap edir; növbəti HTTP sorğuda başqa ID olur. Controller sorğunun müddətini çap edir.

### Tapşırıq 18 (müvəqqəti səhv variant)
`@RequestScope` əvəzinə təkcə `@Scope("request")` yazın. Singleton bean yaranarkən active HTTP request olmadığından scope resolve edilə bilmir: tipik olaraq `ScopeNotActiveException` / `BeanCreationException` (exact stack trace versiyadan asılıdır). Sonra `@RequestScope`-a qayıdın və ya `@Scope(value="request", proxyMode=ScopedProxyMode.TARGET_CLASS)` edin. **Bu səhv variant praktik olaraq sınaqdan keçirilməyib.**

### Tapşırıq 19
`requestContext.getClass()` adətən `...RequestContext$$SpringCGLIB$$...` qaytarır. Singleton real request bean-i deyil, Spring-in **scoped proxy** obyektini saxlayır; proxy hər HTTP request üçün cari `RequestContext`-ə yönləndirir.

## Bölmə 4 — Session

### Tapşırıq 20–22
`RecentlyViewedUsers` `@SessionScope`-dadır; təkrarlanmayan son 5 ID saxlayır (ən yenisi əvvəl). `GET /api/v1/user-info/recent` tam `UserInfoResponse` siyahısı qaytarır.

### Tapşırıq 23
`application.yml`: `server.servlet.session.timeout: 1m`. Session timeout-dan dərhal sonra `@PreDestroy` çıxması **zəmanətli deyil**; servlet konteynerinin session eviction/invalidation vaxtından asılıdır. Yoxlama üçün sessiyanı aktiv edin, bir dəqiqədən çox boş saxlayın və növbəti sorğuda nəticəyə/log-a baxın. **Real log screenshot-u əlavə edin.**

### Tapşırıq 24
Chrome-da `/1`, `/3`, `/2`, `/1` → `/recent`: ID sırası `[1,2,3]` (tam user obyektləri). Incognito-da `/recent`: boş `[]` — ayrıdır. Postman cookie-ləri saxlamırsa, fərqli cookie jar istifadə olunursa və ya `JSESSIONID` göndərilmirsə boş nəticə gələ bilər. Curl üçün yuxarıdakı `cookies.txt` nümunəsinə baxın.

### Tapşırıq 25
Request scope-da siyahı hər request-də sıfırlanardı; növbəti `/recent` sorğusu əvvəlki baxışları bilməzdi. Session scope eyni istifadəçi sessiyası boyunca məlumatı qoruyur.

## Bölmə 5 — Dependency Injection

### Tapşırıq 26
`UserRepository` 3 metodlu interface-dir; `InMemoryUserRepository` onu implement edir.

### Tapşırıq 27 (müvəqqəti səhv variant)
`MockUserRepository`-dəki `@ConditionalOnProperty` və `InMemoryUserRepository`-dəki `@ConditionalOnProperty` annotasiyalarını **müvəqqəti olaraq silin**. İki `UserRepository` bean yaranacaq; `UserInfoService(UserRepository repository, ...)` konkret birini seçə bilmədiyinə görə adətən `NoUniqueBeanDefinitionException` yaranacaq. Real stack trace-ı qeyd edin.

### Tapşırıq 28 (müvəqqəti variant)
Eyni halda `InMemoryUserRepository` üzərinə `@Primary` qoyun; default memory olur. Screenshot çəkin.

### Tapşırıq 29 (müvəqqəti variant)
`@Primary`-ni silin. `UserInfoService` konstruktorunda `UserRepository repository` parametrinə `@Qualifier("mockUserRepository")` yazın. `/all` iki `Mock User` qaytarır. Screenshot çəkin. **Sonra dəyişiklikləri geri alın:** final layihə repository-ni `application.yml` ilə seçir (tapşırıq 36).

### Tapşırıq 30
`NotificationService.sendWelcome(String email)` interface-dir. Optional setter injection: `@Autowired(required=false)` setter üzərindədir, field üzərində deyil. `ConsoleNotificationService`-dəki `@Service` annotasiyasını müvəqqəti silib app-i açın: optional olduğuna görə xəta yoxdur.

### Tapşırıq 31
`@Service`-i qaytarın. POST-dan sonra konsolda `Xos geldin: ...` görünəcək.

### Tapşırıq 32
`UserInfoServiceTest`: Mockito mock-ları və **`new UserInfoService(...)` yalnız unit testdə**; Spring context işləmir. Field injection olsaydı, obyekt Spring tərəfindən yaradılmadığına görə dependency-lər null qalardı; konstruktor injection isə class-ın asılılıqlarını məcburi və test edilə bilən edir. Əsas koda field injection əlavə etməyin.

### Tapşırıq 33
Service `List<UserRepository>` qəbul edir; hazırkı conditional konfiqurasiyada **1 bean** olur. Hər iki annotasiyanı müvəqqəti sildikdə və `@Primary` variantında **2 bean** inject olunur. `@Primary` tək `UserRepository` seçiminə təsir edir, `List` siyahısından digər bean-i silmir.

## Bölmə 6 — Bonus

### Tapşırıq 34
`GET /api/v1/scope-demo`: `singletonHashCode` hər sorğuda sabit, `prototypeAuditId1/2` ayrı, `requestId` hər HTTP sorğuda fərqlidir. Aşağıda Postman-da alınmış **real nəticələr** var (UUID-lər cədvəldə qısaldılıb).

| Sorğu | singletonHashCode | prototypeAuditId1 | prototypeAuditId2 | requestId |
|---|---|---|---|---|
| 1 | 1576499395 | ff513efd… | 460c1766… | b813b3c0 |
| 2 | 1576499395 | 199a9ecf… | b3a87f04… | 44996163 |
| 3 | 1576499395 | 5846fde4… | 81941661… | 23144bd1 |

### Tapşırıq 35
`UserStatistics` `@ApplicationScope` və `AtomicLong` istifadə edir; bütün brauzerlər/istifadəçilər üçün **eyni app instansiyasında** ortaq sayğaclardır. `/api/v1/stats`: `createdUsers` və `getUserByIdCalls`.

### Tapşırıq 36
`application.yml` içində `app.repository-type: memory` (3 ilkin user) və ya `mock` (2 Mock User). `@ConditionalOnProperty` yalnız seçilmiş implementasiyanı yaradır. Dəyişəndən sonra tətbiqi restart edin.

### Tapşırıq 37
`BeanLifecycleLogger` `BeanPostProcessor`-dur; `az.iktex` class-larının initialization-dan **əvvəl/sonra** log yazır. Konstruktor → dependency injection → before initialization → `@PostConstruct` → after initialization ardıcıllığı müşahidə olunur. Scoped proxy/lazy scope-lar bəzi log-ların vaxtına təsir edə bilər. **Konsol screenshot-u əlavə edin.**

## Şifahi müdafiə — qısa cavablar

- **IoC vs DI:** IoC obyektlərin yaradılması/idarəsi üzərində nəzarətin framework-ə verilməsidir; DI həmin nəzarətin tətbiq mexanizmidir — dependencies xaricdən verilir.
- **Constructor injection 3 üstünlük:** (1) məcburi dependency-lər obyekt yarananda mövcud olur, (2) `final` immutable references mümkündür, (3) Spring-siz unit test asandır.
- **Prototype @PreDestroy:** Spring avtomatik çağırmır; obyektin cleanup-ı müştəri koduna aiddir.
- **Request into singleton:** Singleton-a scoped proxy inject edilir, hər request-də cari target instance tapılır.
- **Session vs application:** Session scope yalnız bir istifadəçi sessiyası üçün (son baxılan user-lər); application scope bütün ServletContext üçün (ümumi statistika).
- **@Primary + @Qualifier:** uyğun explicit `@Qualifier` seçimi `@Primary`-dən üstündür (qualifier həmin bean-i seçirsə).

## Praktik yoxlamaların nəticələri (12 oktyabr 2026)

- Başlanğıcda `GET /api/v1/user-info/all` üç istifadəçi qaytardı: Ali Aliyev, Leyla Mammadova və Nigar Hasanli.
- `GET /api/v1/user-info/9999`: **HTTP 404**, `{"error":"User tapilmadi: 9999"}`.
- Singleton: təkrar GET sorğularında controller/service/repository konstruktorları yenidən çağırılmadı.
- Request scope: bir sorğuda controller, service və repository üçün `dcd501f4` eyni idi; sonrakı sorğuların ID-ləri dəyişdi. `RequestContext$$SpringCGLIB$$0` log-u müşahidə edildi.
- Prototype: CREATE_USER audit ID-ləri `c85691e2-…`, `9f91d204-…`, `7f8ef6b8-…` olmaqla fərqli idi. `Xos geldin: …` mesajları da çıxdı.
- Session: Chrome-da `/1`, `/3`, `/2`, `/1` ardıcıllığından sonra `/recent` 1,2,3 qaytardı; Incognito-da `[]` qaytardı.
- Statistics: ardıcıl `getUserByIdCalls` **6 → 7 → 8**, `createdUsers` isə **3** olaraq müşahidə edildi.
- İki repository eyni vaxtda aktiv olduqda Spring start etmədi: `DemoController` üçün `expected single matching bean but found 2: inMemoryUserRepository,mockUserRepository`.
- `@Primary` ilə memory repository seçildi, `/all` üç ilkin user-i qaytardı. `@Qualifier("mockUserRepository")` ilə `/all` iki `Mock User` qaytardı. Daha sonra konfiqurasiya memory seçiminə bərpa edildi.
- `ConsoleNotificationService`-də `@Service` müvəqqəti söndürüldükdə tətbiq yenə açıldı; annotasiya sonradan bərpa edildi.

**Tam yoxlanmamış təcrübələr:** 10 sorğunun dəqiq konstruktor sayımı, tapşırıq 11-də birbaşa prototype injection, tapşırıq 13-də prototype `@PreDestroy`-un sınağı, tapşırıq 18-də proxy-siz request scope xətası, tapşırıq 32-də unit testin icrası və `gradle test` nəticəsi. Bu təcrübələr üçün screenshot iddiası edilmir.

## Screenshot sübutları

`README.md` ilə eyni səviyyədə `screenshots/` qovluğunu yaradın və çəkdiyiniz şəkilləri ora yerləşdirin. Fayllar həqiqətən mövcud olduqda aşağıdakı linklər GitHub-da görünəcək:

| Bölmə | Screenshot faylı | Sübut |
|---|---|---|
| Singleton | `task1-initialization.png` | İlk 3 user-in yüklənməsi |
| Singleton | `task1-all-users.png` | GET `/all` |
| Singleton | `task1-get-user.png` | GET `/1` |
| Singleton | `task1-create-user.png` | POST `/create` |
| Singleton | `task1-get-created-user.png` | Yeni user-in GET ilə yoxlanması (əgər çəkilibsə) |
| Singleton | `task1-user-not-found.png` | HTTP 404 |
| Singleton | `task1-singleton-reuse.png` | Təkrar sorğularda singleton reuse |
| Prototype | `task2-prototype-three-ids.png` | Fərqli audit ID-ləri |
| Request | `task3-request-scope.png` | Eyni sorğuda eyni request ID |
| Session | `task4-session-normal.png` | Son baxılan istifadəçilər |
| Session | `task4-session-incognito.png` | Ayrı sessiyada boş siyahı |
| DI | `task5-di-error.png` | İki repository ambiguity xətası |
| DI | `task5-primary-startup.png` | `@Primary` ilə uğurlu startup |
| DI | `task5-primary-users.png` | Memory user-lər |
| DI | `task5-qualifier-mock.png` | Mock user-lər |
| DI | `task5-optional-di.png` | Optional DI ilə uğurlu startup |
| Bonus | `task6-scope-demo-1.png`, `task6-scope-demo-2.png`, `task6-scope-demo-3.png` | Scope müqayisəsi |
| Bonus | `task6-stats-before.png`, `task6-stats-after.png`, `task6-stats-cross-browser.png` | Sayğacların paylaşılması |

Çəkmədiyiniz faylları uydurmayın; cədvəldəki mövcud olmayan şəkil adlarını silə bilərsiniz.

## GitHub-a təhvil

Layihənin kök qovluğunda `.gitignore` faylına `build/`, `.gradle/`, `.idea/`, `*.iml` və `.DS_Store` əlavə edin. Sonra Terminal-da:

```bash
cd ~/Downloads/spring-bean-scopes-di
./gradlew test
./gradlew build
git init
git add .
git commit -m "Complete Spring bean scopes and DI assignment"
git branch -M main
# GitHub-da boş (README əlavə edilməmiş) repo yaradın; URL-ni aşağıda əvəz edin:
git remote add origin https://github.com/YOUR_USERNAME/spring-bean-scopes-di.git
git push -u origin main
```

**Commit qeydi:** Tapşırıq altı ayrıca mərhələ commit-i tələb edir. Layihə artıq yekun vəziyyətdə qurulduğuna görə keçmiş mərhələ tarixçəsi mövcud deyil. Eyni kod üçün altı saxta commit yaratmaq əvəzinə mövcud işi düzgün commit etmək daha şəffafdır. Bu, tapşırığın həmin qaydasını tam ödəmir.
