# NIVEK: ECLIPSE

Mod Fabric para Minecraft 1.21.8 (Java 21). Um meteoro cai, o jogador se funde com uma forma de vida extraterrestre e evolui.

Desenvolvimento em fases. Antes de continuar, leia `DEVELOPMENT_STATUS.md` e `CONTINUATION.md`.

## Build
- GitHub Actions: a cada push o build roda sozinho; o jar sai em Actions > build > Artifacts e, em push na main/master, tambem em Releases (build-N). Para uma versao fixa, crie uma tag `v*` (ex.: v0.2.0).
- Local: Gradle 8.14+ e JDK 21, `gradle build`. O jar fica em `build/libs/`.

## Controles
- `G`: abre o ECLIPSE SYSTEM (precisa de mutação nível 1). Abas funcionais: OVERVIEW, EVOLUTION, RESEARCH, IMPLANTS, TRANSFORMATION, BESTIARY.
- `V`: Lâmina Biológica. `R`: Garras. 3 golpes seguidos em 2 s fazem o combo final.
- Asas (aba TRANSFORMATION): no ar, segure agachar para planear.

## Comandos de teste (op nível 2)
`/eclipse meteor | status | mutation <0-5> | xp <n> | material <n> | experiment | boss | implants all|none | max | reset`

Atalho para testar tudo: `/eclipse max` (mutação 5, todas as pesquisas e implantes, 99999 material).
