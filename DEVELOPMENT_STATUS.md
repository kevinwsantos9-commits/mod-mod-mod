# DEVELOPMENT_STATUS

- Projeto: NIVEK: ECLIPSE, versão 0.1.0
- Mod id: `nivekeclipse` | Pacote: `com.nivek.eclipse`
- Minecraft: 1.21.8 | Fabric Loader: 0.19.5 | Fabric API: 0.136.1+1.21.8
- Loom: 1.11-SNAPSHOT (Gradle 8.14.3 no CI) | Mappings: oficiais Mojang | Java: 21

## Fases concluídas
- FASE 01 — Fundação (build passou no GitHub Actions, confirmado pelo usuário)

## Revisão estática 2 (sessão de 2026-11-13, sem compilar)
- Log real do GitHub Actions: o ÚNICO erro era `level.isNight()` em `MeteorEvent` (linha 74, `ServerLevel` não tem o método). Esse trecho JÁ estava corrigido neste zip (`isNightTime(level)`); o log era de um commit anterior. Como `javac` para no primeiro conjunto de erros, o source set `client` ainda não tinha sido compilado: podem aparecer erros novos depois que o `main` passar.
- Relidos contra a API 1.21.8 (Mojang mappings + Fabric API 0.136.1): `ImplantManager/Slot/Payload`, `ResearchManager/Payload`, `EvolutionManager`, `CreatureManager`, `ModAttachments`, `EclipseCommand`, `ImplantModel/Layer`, `EclipseRenderState`, os 2 mixins, `NivekEclipseClient`, `EclipseHud`, `ClientCombatState`, `EclipseScreen/Tab`, `build.gradle`, `fabric.mod.json`, workflow. Nenhum erro CERTO de compilação encontrado; nada foi alterado nesses arquivos.
- Riscos que continuam sem confirmação (conferir primeiro se o build falhar): `LivingEntityFeatureRendererRegistrationCallback` (assinatura de `registerRenderers` e `helper.register`), `HudRenderCallback` (depreciado), descriptor do mixin `extractRenderState`, `syncWith` + `AttachmentSyncPredicate`, `Entity.hurtMarked/push`, `new Zombie(Level)` / `new Spider(EntityType, Level)`.
- Novo comando de teste: `/eclipse max` (mutação 5, XP 0, 99999 material, todas as pesquisas e implantes, energia cheia, meteoro marcado como feito). Só em `EclipseCommand` (não verificado).
- Pausa: Fase 09.2 (chefes) NÃO iniciada. Pré-requisito: build verde das Fases 02 a 09.1.

## Verificação automática (sessão de 2026-11-13, sem compilar)
- `tools/static_check.py` (rodar na raiz: `python3 tools/static_check.py`): confere chaves/parênteses balanceados e classes usadas sem `import`. Resultado nesta sessão: nenhum problema real (os avisos "sem import?" de `Type`, `Entry`, `Operation`, `Properties`, `Types`, `BossBarColor`, etc. são falsos positivos: classes aninhadas ou nomes qualificados por extenso). Nenhum import não usado.
- Todos os JSON (fabric.mod.json, mixins, lang, blockstate, modelos, item) são válidos; texturas referenciadas existem. Isto NÃO substitui o build: não detecta método inexistente nem tipo errado.

## Fase atual (11.1)
FASE 11 — Finalização, etapa 11.1 escrita (NÃO compilada, NÃO testada). Pré-requisito pendente: build verde das Fases 02 a 11.1. Otimização, balanceamento e testes finais NÃO feitos (dependem do build verde e de teste em jogo).

- Fase 11 etapa 11.1 (não verificada): `CombatManager.isValidTarget` — Lâmina e Garras não ferem mais outros jogadores, suportes de armadura nem pets (`TamableAnimal.isOwnedBy`) do jogador (resolve a limitação "golpes podem ferir aliados/jogadores"); README ganhou seções Controles e Comandos de teste
- Limitações 11.1: pets não-`TamableAnimal` (cavalos, lobos de outros donos) e aldeões ainda podem ser atingidos; sem opção de PvP configurável. Riscos de API a conferir: `TamableAnimal.isOwnedBy(LivingEntity)`

## Fase atual (10.3)
FASE 10 — Transformações, etapa 10.3 escrita (NÃO compilada, NÃO testada). Pausa de fase: animação de transição entre formas, formas avançadas extras e transformação final cinematográfica NÃO iniciadas. Pré-requisito pendente: build verde das Fases 02 a 10.3.

- Fase 10 etapa 10.3 (não verificada): planeio das Asas em `FormManager.glide` (formas Asas e Eclipse): no ar, segurando agachar, a queda é limitada a 0,1 bloco/tick, o jogador avança na direção do olhar (+0,03 por tick, máx. 0,6 horizontal), a distância de queda é zerada; velocidade enviada ao cliente via `hurtMarked`. Roda todo tick só para jogadores com a forma
- Limitações 10.3: não é voo livre (só planeio descendente), pode haver tremor porque o cliente controla o movimento do jogador, sem custo extra de energia além do upkeep, sem animação própria de planeio, sem balanceamento. Riscos de API a conferir: `Entity.onGround()`, `Entity.isShiftKeyDown()`, `Entity.getLookAngle()`, `Entity.resetFallDistance()`, `Entity.hurtMarked`

## Fase atual (10.2)
FASE 10 — Transformações, etapa 10.2 escrita (NÃO compilada, NÃO testada). Pausa de fase: asas com voo real, formas avançadas extras e transformação final animada (10.3) NÃO iniciadas. Pré-requisito pendente: build verde das Fases 02 a 10.2.

- Fase 10 etapa 10.2 (não verificada): visual das formas como `RenderLayer` (`FormLayer`) com modelo de caixas (`FormModel`: tentáculo, asas esquerda/direita, ombreiras, coroa). Tentáculos: 4 nas costas balançando em fases diferentes (seno de `ageInTicks`); Asas: 2 abertas nas costas batendo; Armadura: ombreiras nos dois braços + coroa na cabeça; Forma Eclipse: tudo junto. Índice da forma passa ao `PlayerRenderState` por mixin (novos get/set em `EclipseRenderState`; `PlayerRendererMixin` lê `EclipseData.getForm`); visível para todos os jogadores com mutação >= 1
- Limitações 10.2: peças são caixas provisórias com a textura do braço (`bio_arm.png`), sem emissividade, sem animação de transição ao ativar/desativar, ombreiras podem desalinhar ~1px em skin slim, não escondem sob armadura. Riscos de API a conferir: `ModelPart.setPos/setRotation`, `PlayerRenderState.ageInTicks`, `model.rightArm`

## Fase atual (10.1)
FASE 10 — Transformações, etapa 10.1 escrita (NÃO compilada, NÃO testada). Pausa de fase: visual 3D das formas (tentáculos, asas, armadura), formas avançadas extras e transformação final animada NÃO iniciados. Pré-requisito pendente: build verde das Fases 02 a 10.1.

- Fase 10 etapa 10.1 (não verificada): `transformation/Form` (4 formas: Tentáculos m3, Armadura Eclipse m4, Asas m4, Forma Eclipse m5), `FormPayload` (C2S, alterna; repetir a forma ativa desativa), `FormManager` (valida mutação e energia mínima 20; uma forma por vez; custo de energia por segundo: 6/6/5/8; a cada 20 ticks aplica modificadores de atributo permanentes idempotentes com ids por forma, desfaz se faltar energia ou mutação; partículas a cada 5 ticks; reaproveita `ImplantManager.setModifier`). Efeitos: Tentáculos +4 dano, +1,5 alcance (`ENTITY_INTERACTION_RANGE`); Armadura +8 armadura, +4 resistência, +0,5 repulsão; Asas +15% velocidade, +10 queda segura, queda lenta; Eclipse: tudo mais forte. Attachment `active_form` (int, -1 = nenhuma; persistente, NÃO copia na morte, sync com todos para visual futuro). Aba TRANSFORMATION funcional (clique alterna); `/eclipse reset` desfaz a forma
- Limitações 10.1: sem modelo/animação das formas (só partículas), asas não voam (só queda lenta), sem tecla de atalho, sem balanceamento (regen de energia é +4/s, então Tentáculos/Armadura/Asas drenam ~2/s, Eclipse ~4/s líquido), sem cooldown ao trocar. Riscos de API a conferir: `Attributes.ENTITY_INTERACTION_RANGE/ARMOR_TOUGHNESS`, `SoundEvents.BEACON_ACTIVATE`, `MobEffects.SLOW_FALLING`

## Fase atual (9.2)
FASE 09 — Criaturas e chefes, etapa 9.2 escrita (NÃO compilada, NÃO testada). Pausa de fase: entidades próprias (modelo/IA/animação), mais chefes e Fase 10 NÃO iniciadas. Pré-requisito pendente: build verde das Fases 02 a 09.2.

- Fase 09 etapa 9.2 (não verificada): `creature/BossManager` — "Experimento Alfa" = Ravager reforçado (vida 150+50*mutação, nome visível, brilho, persistente, tag `nivekeclipse_boss`) com `ServerBossEvent` (roxa, 10 divisões) mostrada a jogadores até 64 blocos; 3 fases: normal, <66% velocidade + rugido, <33% barra vermelha e 2 experimentos de reforço (`CreatureManager.spawnNear`); surge a cada ~30 s com chance 1/8 para jogador com mutação >= 3, Overworld, noite, só 1 por jogador; morte por jogador dá +60..90 material, +300 XP, orbes de XP (40) e +1 em `boss_kills` (Attachment novo, sync só com o dono); aba BESTIARY mostra chefes derrotados; `/eclipse boss` força um; `/eclipse reset` zera `boss_kills`
- Limitações 9.2: não é entidade própria (sem modelo/IA/animação nova), sem drops de itens, sem balanceamento, chefe some se o servidor parar (estado não persiste; entidade persiste no mundo mas perde a barra), um tipo de chefe só. Riscos de API a conferir: `new Ravager(EntityType, Level)`, `ServerBossEvent.getPlayers/addPlayer/removePlayer/removeAllPlayers/setColor/setProgress`, `Mob.setPersistenceRequired`, `ExperienceOrb.award(ServerLevel, Vec3, int)`, `SoundEvents.RAVAGER_ROAR`

## Fase atual (atualizado 6)
FASE 09 — Criaturas e chefes, etapa 9.1 escrita (NÃO compilada, NÃO testada). Pausa de fase: chefes (9.2) e entidades próprias com modelo/IA/animação NÃO iniciados. Pré-requisito pendente: build verde das Fases 02 a 09.1.

- Fase 09 etapa 9.1 (não verificada): `CreatureManager` — "Experimentos" são mobs vanilla alterados (zumbi "Experimento Falho", aranha "Experimento Rastejante") com vida (20+10*mutação) e dano (3+mutação) reforçados, nome visível, brilho, tag `nivekeclipse_experiment` e alvo travado no jogador; surgem a cada ~30 s com chance 1/3 perto (20-30 blocos) de jogadores com mutação >= 1, só no Overworld à noite, máx. 4 por perto; abate dá +5..8 material e +30 XP e conta no Attachment `experiment_kills` (sync só com o dono); aba BESTIARY mostra abates e as duas criaturas; `/eclipse experiment` força um; `/eclipse reset` zera abates; `MeteorEvent.isNightTime` agora é público
- Limitações 9.1: não são entidades próprias (sem modelo/textura/IA nova), sem chefes, sem drops físicos, aparecem só à noite (zumbi queima de dia), sem balanceamento. Riscos de API a conferir: `new Zombie(Level)`, `new Spider(EntityType, Level)`, `Entity.setGlowingTag`, `Mob.setTarget/setCanPickUpLoot`, `Level.hasChunkAt(BlockPos)`

## Fase atual (8.3)
FASE 08 — Evolução, etapa 8.3 escrita (NÃO compilada, NÃO testada). Fase 08 completa em código, exceto árvore de habilidades ramificada. Pausa de fase: Fase 09 NÃO iniciada. Pré-requisito pendente: build verde das Fases 02 a 08.3.

- Fase 08 etapa 8.3 (não verificada): 5 pesquisas permanentes (`Research`), compradas com material na aba RESEARCH (clique), validadas no servidor (`ResearchPayload`, `ResearchManager`): Tecido Regenerativo (m2, 40: +4 vida máx), Núcleo de Energia (m2, 50: +2 regen de energia), Fibras Densas (m3, 70: +0,3 resistência a repulsão), Instinto Predador (m3, 90: +25% dano de Lâmina/Garras), Metabolismo Evoluído (m4, 120: +50% XP); Attachment `research` (máscara, persistente, copyOnDeath, sync só com o dono); `/eclipse reset` zera
- Limitações 8.3: sem árvore ramificada (lista simples, sem pré-requisitos entre pesquisas), sem reembolso/respec, sem balanceamento
- Workflow: `.github/workflows/build.yml` agora compila a cada push/PR, guarda o jar em Artifacts e cria Release automática (`build-N` em push na main/master; nome da tag em tags `v*`)

## Fase atual (8.2)
FASE 08 — Evolução, etapa 8.2 escrita (NÃO compilada, NÃO testada). Pausa de fase: pesquisas e árvore de habilidades (8.3) NÃO iniciadas. Pré-requisito pendente: build verde das Fases 02 a 08.2.

- Fase 08 etapa 8.2 (não verificada): "Material Eclipse" como contador por jogador (Attachment `materials`, persistente, copyOnDeath, sync só com o dono; NÃO é item); matar mobs com mutação >= 1 dá material (hostis 1-3, outros 0-1) em `EvolutionManager.addMaterials`; instalar implante custa material (`ImplantSlot.cost`: cabeça 10, braços 15, costas 20, pernas 25, peitoral 30) e remover devolve o custo; aba IMPLANTS e OVERVIEW mostram o material; `/eclipse material <n>`; `/eclipse reset` zera; `/eclipse implants all` ignora custo (teste)
- Limitações 8.2: sem itens/drops físicos, sem outros tipos de material, valores sem balanceamento; `/eclipse implants none` não devolve material

## Fase atual (8.1)
FASE 08 — Evolução, etapa 8.1 escrita (NÃO compilada, NÃO testada). Pausa de fase: pesquisas e materiais (8.2/8.3) NÃO iniciados. Pré-requisito pendente: build verde das Fases 02 a 08.1.

- Fase 08 etapa 8.1 (não verificada): os 5 estágios = nível de mutação 1..5; Attachment `evolution_xp` (progresso rumo ao próximo estágio; persistente, copyOnDeath, sync só com o dono); `EvolutionManager`: XP ao matar mobs (vida máxima; x2 para `Enemy`; só jogador com mutação >= 1 e < 5; usa `ServerLivingEntityEvents.AFTER_DEATH`), custo 100/300/700/1500 XP por estágio, evolução automática com mensagem, partículas e som; aba EVOLUTION funcional (estágio, barra de XP, lista dos 5 estágios); `/eclipse xp <n>` para teste; `/eclipse reset` zera o XP
- Limitações 8.1: sem pesquisas, materiais nem árvore de habilidades (8.2/8.3); evolução não tem animação própria além de partículas; estágios 2 a 5 ainda não mudam o visual; valores de XP sem balanceamento; risco de API a conferir: `ServerLivingEntityEvents.AFTER_DEATH`, `Enemy`, `SoundEvents.BEACON_POWER_SELECT`

## Fase atual (7.2)
FASE 07 — Implantes, etapa 7.2 escrita (NÃO compilada, NÃO testada). Pausa de fase: nada além da 7.2 foi iniciado. Pré-requisito pendente: build verde das Fases 02 a 07.2.

- Fase 07 etapa 7.2 (não verificada): visual dos implantes como `RenderLayer` (`ImplantLayer`) com modelo de caixas (`ImplantModel`: visor na cabeça, placa no peitoral, módulo nas costas, manga no braço esquerdo, caneleiras nas duas pernas), desenhados sobre as partes do `PlayerModel` (`head/body/leftArm/leftLeg/rightLeg.translateAndRotate`); máscara de implantes passa ao `PlayerRenderState` por mixin (novos get/set em `EclipseRenderState`); só renderiza com mutação >= 1; visível para todos os jogadores
- Limitações 7.2: peças são caixas provisórias, usam a textura do braço (`textures/entity/bio_arm.png`), sem emissividade, sem animação, não escondem sob armadura, braço slim pode desalinhar ~1px

## Fase atual (7.1)
FASE 07 — Implantes, etapa 7.1 escrita (NÃO compilada, NÃO testada). Pausa de fase: nada além da 7.1 foi iniciado. Pré-requisito pendente: build verde das Fases 02 a 06.2.

- Fase 07 etapa 7.1 (não verificada): 5 slots (`ImplantSlot`: cabeça, braços, costas, pernas, peitoral) com mutação mínima por slot (1,1,2,2,3); Attachment `implants` (máscara de bits, persistente, copyOnDeath, sync com todos); `ImplantPayload` (C2S, alterna instalar/remover; servidor valida mutação); `ImplantManager` aplica efeitos a cada 20 ticks: cabeça = visão noturna; braços +2 ataque; costas +3 queda segura; pernas +10% velocidade e +0,1 pulo; peitoral +4 armadura (modificadores permanentes idempotentes, só valem com mutação >= 1); aba IMPLANTS funcional (clique alterna); comandos `/eclipse implants all|none`; `/eclipse reset` zera implantes
- Limitações 7.1: sem custo/material (Fase 08), sem visual 3D dos implantes (7.2), visão noturna some ~15 s depois de remover a cabeça, valores sem balanceamento. Riscos de API a conferir no build: `Attributes.SAFE_FALL_DISTANCE`/`JUMP_STRENGTH`, `AttributeInstance.hasModifier/addPermanentModifier/removeModifier(ResourceLocation)`, `new MobEffectInstance(holder, int, int, bool, bool, bool)`

## Fase atual (anterior)
FASE 06 — Combate, etapa 6.2 escrita (aguardando build). As Fases 02, 03, 04.1, 05.1 e 06.1 também NÃO tiveram o build confirmado (usuário mandou seguir).

## Fases pendentes
04 ECLIPSE SYSTEM, 04 (conteúdo das abas), 05 (refinamento do braço, ver abaixo), 06 (ataque carregado, opcional), 07 Implantes, 08 Evolução, 09 Criaturas e chefes, 10 Transformações, 11 Finalização

## Funcionalidades implementadas
- Fase 01: projeto Gradle/Loom, entrypoints, workflow
- Fase 02 (não verificada): bloco/item Núcleo Alienígena (textura gerada, lang en/pt_br), dado por jogador `meteor_event_done` (Attachment persistente), gatilho (noite, céu aberto, Overworld, ~1/30 a cada 10 s), queda simulada no servidor com partículas e sons, cratera (raio 7, profundidade 4), núcleo no fundo, comandos `/eclipse meteor` e `/eclipse reset` (op 2)

- Fase 03 (não verificada): ao tocar o núcleo, sequência de 5 s (jogador preso no lugar, 6 tentáculos de partículas, sons, partículas de portal); no fim concede mutação nível 1 (Attachment `mutation_level`, persistente, copyOnDeath) e consome o núcleo; cancela sem perdas se morrer/desconectar; `/eclipse status` e `/eclipse reset` (agora zera também a mutação)

- Fase 04 etapa 4.1 (não verificada): `mutation_level` sincroniza com o cliente (syncWith VAR_INT, targetOnly); tecla G abre o ECLIPSE SYSTEM (só com mutação >= 1); tela com 11 abas e navegação lateral por clique; aba OVERVIEW funcional (jogador, estágio, mutação x/5, barra); demais abas são placeholders; `/eclipse mutation <0-5>` para teste

- Fase 05 etapa 5.1 (não verificada): braço biomecânico (ombro, antebraço, garras) desenhado como RenderLayer sobre o braço direito de qualquer jogador com mutação >= 1 (mutação agora sincroniza com todos os clientes); textura 64x32 gerada por script; animações: idle (pulso + dedos), transformação (cresce em 40 ticks quando a mutação passa de 0 para >=1), ataque (garras abrem com `attackTime`); 2 mixins client (PlayerRenderState ganha dados extras; PlayerRenderer.extractRenderState preenche)

- Fase 06 etapa 6.1 (não verificada): energia Eclipse 0-100 (persistente, sync só com o dono, regen +2 a cada 10 ticks com mutação >= 1), pacote cliente->servidor `AbilityPayload`, `CombatManager` (valida mutação, cooldown 30 ticks, custo 20); Lâmina Biológica (tecla V): golpe em cone de 3,5 blocos, 8 de dano, partículas (varredura + arco de chamas de alma) e som; barra de energia na aba OVERVIEW

- Fase 06 etapa 6.2 (não verificada): Garras (tecla R: custo 8, recarga 10 ticks, 4 de dano, alcance 2,8, riscos de partículas CRIT); combos (janela de 40 ticks, 3 golpes: x1.0, x1.25, x1.5 + golpe final com repulsão e explosão visual); `CooldownPayload` servidor->cliente; HUD no canto inferior direito (barra de energia + recarga de V e R); `AbilityDef` em `CombatManager`

## Bugs conhecidos / limitações
- Fase 06.2: ataque carregado NÃO implementado; HUD usa `HudRenderCallback` (depreciado em versões novas do Fabric API); golpes não ferem mais jogadores/pets (11.1); garras sem modelo/animação 3D própria; sem balanceamento
- Fase 05: modelo simples (caixas), pensado para braço clássico (4px; skin slim pode desalinhar 1px); só o braço direito; textura procedural provisória; sem emissividade; animações simples. Mixin com defaultRequire=1: se o descriptor estiver errado, o jogo falha ao abrir com erro claro. `ArmTransformTracker` não limpa entradas de quem sai (vazamento pequeno)
- Fase 04: abas além de OVERVIEW só mostram "Disponivel em breve" (o conteúdo depende dos sistemas das Fases 05–10); sem animações/ícones/texturas de UI; sem toggle da tecla G dentro da tela (ESC fecha)
- Fusão: sem invulnerabilidade durante a sequência; qualquer jogador sem mutação pode usar o núcleo
- Fusão: tentáculos são só partículas (sem modelo); sem câmera cinematográfica (exige código de cliente)
- Meteoro é só partículas (sem entidade/modelo): visível apenas a ~32 blocos
- Sem tremor de tela (exige pacote de rede)
- Núcleo só mostra mensagem ao toque; fusão é a Fase 03
- Núcleo é indestrutível em survival (strength -1)

## Último resultado da compilação
Fases 02, 03, 04.1, 05.1, 06.1 e 06.2: AINDA NÃO COMPILADAS (nenhuma IA até aqui teve Gradle/Maven disponível).

## Revisão estática (sessão de 2026-11-13, sem compilar)
Todo o código das Fases 02 a 06.2 foi relido contra a API da 1.21.8 (Mojang mappings + Fabric API 0.136.1). Não foi encontrado nenhum erro de compilação certo. Ajustes feitos:
- `MeteorEvent`: `level.isNight()` trocado por checagem de `getDayTime()` (13000..23000), porque `isNight` foi renomeado em versões recentes e não deu para conferir na 1.21.8.
- `MeteorEvent`, `FusionEvent`, `CombatManager`: estado estático limpo em `ServerLifecycleEvents.SERVER_STOPPED` (evita segurar `ServerLevel` antigo ao trocar de mundo).
- Cliente: `ArmTransformTracker.clear()` em `ClientPlayConnectionEvents.DISCONNECT` (resolve o vazamento citado nas limitações da Fase 05).
Pontos ainda NÃO confirmados (conferir primeiro se o build falhar): assinatura do callback de `LivingEntityFeatureRendererRegistrationCallback` (`helper.register`), `HudRenderCallback` (depreciado), `Entity.hurtMarked`/`push`, descriptor do mixin `extractRenderState`, `syncWith` + `AttachmentSyncPredicate`.
Observação: o código da 6.2 (Garras, combos, HUD, `CooldownPayload`) já estava neste zip. Não foi escrito nada novo da 6.2 nesta sessão; a próxima etapa só começa depois do build verde.
