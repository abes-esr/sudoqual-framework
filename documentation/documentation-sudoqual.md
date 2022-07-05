# Introduction

*Le framework d'applications SudoQual propose un cadre pour la*
*réalisation d'applications dédiées au problème d'Entity* *resolution.
Le problème d'Entity* *resolution consiste à trouver les*
*enregistrements qui réfèrent à la même **entité** à travers un
ensemble* *de données qui peuvent provenir de différentes sources et/ou
être* *de différentes natures. Ce problème est également référencé sous
les* *appellations "record* *linkage", "entity disambiguation",
"duplicate detection", "record* *matching", "instance identification",
"deduplication", "coreference* *resolution", "reference reconciliation",
"data linkage"…*

## Historique

L’Abes a participé au projet de recherche Qualinca financé par l’Agence
Nationale de la Recherche. Le projet de recherche Qualinca (2012-2016) a
permis de questionner en profondeur les problèmes de qualité du liage au
sein de la base documentaire du Sudoc. Mesurant l’ampleur des erreurs et
des lacunes concernant les liens présents dans le catalogue Sudoc,
l’Abes a collaboré étroitement avec des chercheurs du LIRMM pour mettre
au point la première version de SudoQual, un prototype logiciel dédié au
problème de résolution de co-référence pour répondre aux besoins
suivants :

-   **liage** : calcul de nouveaux liens (biblio-autorité), alignement
    de référentiels (autorité-autorité), regroupement (biblio-biblio)… ;
-   **diagnostic de l’existant** : détection et correction de liens
    erronés, validation de liens, détection de doublons d’autorité…

En 2019, l'Abes a fait appel à un prestataire de service afin de
réaliser une version « stable » de l'outil et accompagner la mise en
production de celui-ci. Cette prestation a permis la réalisation de la
version actuelle de SudoQual.

## Problématique

SudoQual est un « framework » dédié au problème de « résolution de
co-références » aussi appelé « résolution d'entités », en anglais
« coreference resolution », « entity resolution », « record linkage »,
« entity disambiguation », « duplicate detection », « record matching »,
« instance identification », « deduplication », « reference
reconciliation », « data linkage »…

Ce problème considère deux ensembles d'enregistrement pour lesquels un
enregistrement du première ensemble et un enregistrement du second
peuvent éventuellement représenter la même entité. Appelons
A<sub>1</sub>,…,A<sub>n</sub> les n enregistrement du première ensemble
et B<sub>1</sub>,…,B<sub>m</sub> les m enregistrement du second. On
cherche à obtenir l'ensemble des couples (A<sub>i</sub>,B<sub>j</sub>)
tel que A<sub>i</sub> et B<sub>j</sub> soient deux références à la même
entité. Ce problème étant un problème non-trivial pour lequel il
n'existe pas de méthode exacte, nous l'approcherons en fournissant trois
ensembles de couples (A<sub>i</sub>,B<sub>j</sub>) :

-   un ensemble de couples **sameAs** : les enregistrement d'un même
    couple représentent la même entité ;
-   un ensemble de couples **diffFrom** : les enregistrement d'un même
    couple **ne** représentent **pas** la même entité ;
-   un ensemble de couples **suggestedSameAs** : les enregistrement d'un
    même couple présentent des indices de co-référence.

Dans la suite de ce document, nous décomposerons un enregistrement en
une **référence** à une entité et un **ensemble de données associés**.
Les données associées peuvent représenter directement l'entité, par
exemple pour une référence à une entité de type « personne physique » :
son nom, prénom, date de naissance… Mais elles peuvent aussi représenter
le contexte dans lequel la référence est présente, par exemple pour une
référence à un auteur dans une notice bibliographique : le titre de
l'ouvrage, le nom des co-auteurs, la date de publication…

Dans le cadre des applications de SudoQual aux problématiques de l'Abes (voir dépôt associé : https://github.com/abes-esr/qualinka-sudoc),
nous nous intéresserons à des références d'entités de type :

-   personnes physiques (auteurs, illustrateurs…) ;
-   personnes morales (organisations) ;
-   œuvres (créations intellectuelles/artistiques).

## Références

-   <https://en.wikipedia.org/wiki/Entity_resolution>
-   Fellegi Ivan, Sunter Alan. “A THEORY FOR RECORD LINKAGE\*.” (1969).
-   Le-Provost Aline, Nicolas Yann and Mistral François. “Lier à IdRef.
    Un réseau pour la consultation et la production de données
    d'autorité, une collaboration entre les hommes et les machines.”
    (2017).
-   Chein Michel, Alain Gutierrez and Leclère Michel. “Un problème
    d'identification d'entités nommées dans des bases de donnés
    documentaires.” (2015).
-   Chein Michel, Leclère Michel and Nicolas Yann. “SudocAD: A
    Knowledge-Based System for the Author Linkage Problem.” KSE (2013).

# Exemple de cas d'utilisation par l'Abes et ses partenaires

*Différents cas d'utilisation décrits par l'Abes ont orientés la conception du framework SudoQual. 
Cette section les explicite avec un point de vue métier. 
Ils sont spécifiques aux problèmes de liage rencontrés par l'Abes et ses partenaires et constituent des exemples qu'il nous a semblé intéressant de présenter ici. Chaque utilisateur du framework SudoQual pourra définir ses propres cas d'utilisation. 
Ils peuvent nécessiter le recours à des briques
logicielles externes à SudoQual ou l'implémentation d'enchaînements
spécifiques de différentes configurations de SudoQual pour répondre aux
cas d'utilisation exprimés. Cette section est un extrait du document
« SudoQual - cas d’utilisation ».*

**Vocabulaire utilisé à l'Abes : référence contextuelle (RC) et référence d'autorité (RA)**
Pour comprendre les explications suivantes, il faut connaître la notion de RC et de RA.
- Une **RA** ou référence d’autorité est une référence d'entité dans un référentiel (par exemple une base d'autorité dans le monde de la description bibliographique).
- Une **RC** ou référence contextuelle est une référence d'entité dans le contexte particulier d'une ressource documentaire.

## Diagnostic/réparation d'une base documentaire

### Diagnostic des liens RC-RA pour une appellation

L'utilisateur cherche à obtenir des propositions de corrections des
liens existants entre un ensemble de RC et un ensemble de RA.

1.  L'utilisateur fournira :

    -   un ensemble de RC,
    -   un ensemble de RA,
    -   un ensemble de données pour chaque RC et RA,
    -   un ensemble de liens initiaux (liens existants dans la base
        documentaire),
    -   un ensemble de liens sûrs (liens qui ne seront pas remis en
        cause)

2.  L'utilisateur souhaite obtenir :  
    un « diagnostic » pour chaque RC

    présent dans l'ensemble des RC fournit privé de l'ensemble des RC
    pour lesquelles un lien sûr (sameAs) a été fournit. Les possibilités
    de diagnostic pour une RC sont les suivants :

    -   **Validated** - le lien existant est validé par le système ;
    -   **AlmostValidated** - le lien existant est jugé probable par le
        système ;
    -   **Doutbful** - le lien existant est jugé douteux par le
        système ;
    -   **Erroneous** - le lien existant est invalidé par le système ;
    -   **Missing** - pas de lien dans les données initiales.

    Ce diagnostic est potentiellement accompagné d'une recommendation
    « forte » d'un lien (sameAs), d'une recommendation « faible » de
    liens (suggestedSameAs) et/ou d'une liste de liens déconseillés
    (diffFrom).

### Diagnostic interactif d'une appellation (*cas Paprika*)

L'utilisateur souhaite nettoyer et maintenir la qualité d'une base
documentaire à travers une interface conviviale.

1.  L'utilisateur fournira :

    -   un nom
    -   un prénom

2.  L'utilisateur souhaite obtenir :

    Une visualisation efficiente des résultats du scénario précédent
    (cf. [Diagnostic des liens RC-RA pour une appelation](#diagnostic-des-liens-rc-ra-pour-une-appellation)).

3.  L'utilisateur souhaite pouvoir :

    -   consulter, éditer et valider le résultat
    -   ré-exécuter le « Diagnostic des liens RC-RA » après avoir éditer
        le résultat précédent (ajout de lien sûr *sameAs* ou
        *diffFrom*).

### Détection de doublons, d'autorités manquantes, d'identités mêlées…

L'utilisateur souhaite nettoyer les notices d'autorité d'une appellation
dans une base documentaire.

1.  L'utilisateur fournira :

    -   une liste de RC
    -   une liste de RA
    -   un ensemble de données pour chaque RC et RA,
    -   un ensemble de liens sûrs (liens qui ne seront pas remis en
        cause)

2.  L'utilisateur souhaite obtenir (proposition) :

    -   un diagnostic pour chaque RA :
        -   **Clean**
        -   **Duplicate** + liste des duplicatas
        -   **MixedIdentity** + liste des clusters de RC + preuve de non
            co-référence entre les clusters avec *Why*
        -   **PossibleMixedIdentity** + liste des clusters de RC
    -   Pour chaque cluster de RC sans RA :
        -   **PossibleMissingAuthority** + liste des RC du cluster \[+
            suggestions de RA\]
        -   **MissingAuthority** + liste des RC du cluster.

3.  Exemple

    La base d’autorités IdRef comporte des doublons d’autorités (2
    notices d’autorités réfèrent à une même entité) et des identités
    mêlées (une notice d’autorité réfère à plusieurs entités), ainsi
    qu’un déficit de notices d’autorités au regard des catalogues
    documentaires qui y sont adossés (des entités ne sont pas
    référencées).

    Diagnostiquer les autorités de la base IdRef consiste à repérer les
    doublons, les identités mêlées. Pour un catalogue documentaire
    donné, cela permettra d’évaluer le déficit d’autorité, en suggérant
    la création d’autorités nouvelles pour tel ou tel document.

## Aide au catalogage

### Proposition d'identifiants d'autorités à partir d'un référence contextuelle à une notice bibliographique

1.  L'utilisateur fournira :

    -   une référence et son contexte
        -   titre
        -   keyword
        -   collectivité
        -   …
        -   pour chaque RC :
            -   co-auteurs
            -   nom
            -   prénom
            -   …
    -   une base d'autorité
    -   une ou plusieurs bases documentaires liées

2.  L'utilisateur souhaite obtenir :

    -   pour chaque appellation :
        -   une RA validée

        ET/OU
        -   une liste de RA candidates ordonnées

3.  Exemple

    Une application tierce désirant exploiter les identifiants IdRef
    peut, via un web service, fournir en entrée une notice
    bibliographique et récupérer des propositions d’identifiants IdRef,
    ainsi que les métadonnées associées.

    Ce service ne permet pas l’écriture dans IdRef.

### Proposition d'identifiants d'IdRef lors de la saisie d'une notice bibliographique (*cas STAR*)

C'est un cas particulier du cas précédent où la base d'autorités est
IdRef et la base liée est le Sudoc. La fonctionnalité serait intégrée à
IdRef.

1.  Exemple

    STAR est une application web permettant aux établissements de
    soutenance de déposer les thèses électroniques ainsi que leurs
    métadonnées dans un réservoir national, à partir duquel sont
    déployés des services de signalement, de diffusion et d’archivage de
    long terme. Entre autres métadonnées obligatoires, les opérateurs
    doivent identifier l’autorité IdRef correspondant au doctorant et au
    directeur. Les liens aux autorités sont facultatifs pour l’ensemble
    du jury.

    Pour trouver la bonne autorité, l’opérateur lance une recherche
    depuis STAR qui prend pour paramètres le nom et le prénom et bascule
    automatiquement vers l’application IdRef. La recherche s’accompagne
    de l’envoie d’une partie des métadonnées de la notice
    bibliographique.

    Au sein de l’application IdRef, l’opérateur obtient une liste
    ordonnée de résultats pertinents (par exemple, avec des étoiles :
    plus il y a d’étoiles, plus l’autorité est pertinente). Cette liste
    peut être vide dans le cas où aucune autorité ne correspond.
    L’opérateur peut choisir la bonne autorité en cliquant sur un bouton
    ad hoc et basculer à nouveau sur la page de STAR, qui enregistre le
    lien ainsi créé dans sa base bibliographique. S’il ne trouve pas
    l’autorité, il utilise un formulaire web pour saisir les
    informations nécessaires à la création d’une nouvelle autorité. Dans
    ce cas, certains feature de la RC à lier à une nouvelle autorité
    seront utilisés pour pré-remplir le formulaire d’IdRef.

## Prestation d'identification d'autorités

### Prestation d'identification d'autorités par publications (*cas CONDITOR et SIGAPS*)

On cherche à identifier l'ensemble des autorités apparaissant dans une
liste de publications.

1.  L'utilisateur fournira :

    -   une liste de publications
    -   une liste de base d'autorités
    -   une ou plusieurs bases documentaires liées à la base d'autorités
    -   OPTIONNEL: une liste d'appellation (*Restreint la recherche à
        ces appellation*)
    -   OPTIONNEL: informations supplémentaires pour chaque appellation

2.  L'utilisateur souhaite obtenir :

    -   une liste de quadruplet *\< appellation , publication, id base
        d'autorités, id autorité\>*

3.  Exemples

    L’Abes reçoit des lots de publications provenant de base
    documentaires variées et propose un service d’alignement des
    contributeurs de ces publications avec les identifiants IdRef.

    1.  Exemple 1

        Dans le cadre du projet Conditor, dont l’objectif est de
        référencer la production scientifique publique française, il
        s’agira de rattacher au référentiel IdRef les publiants français
        mentionnés dans différentes sources de données (e.g. Hal,
        Scopus, Wos…).

    2.  Exemple 2

        L’évaluation de la production scientifique des établissements
        est un indicateur important de l’activité de recherche, à
        laquelle est de plus en plus conditionnée l’allocation de
        moyens. Les CHRU disposent aujourd’hui du logiciel de
        bibliométrie Sigaps adossé à la base de données Medline. Avant
        toute chose, chaque CHRU a besoin d’identifier les publications
        associées à ses chercheurs. Cette identification peut être
        facilitée par l’exploitation des identifiants IdRef.

        Le CHRU de Lille a déjà fait appel au service d’alignement
        proposé par l’Abes pour l’aider à identifier ses chercheurs dans
        le référentiel IdRef. Il a fourni une extraction de la base
        Sigaps, ainsi que la liste des chercheurs de l’établissement.

        Finalement, pour chaque publication, SudoQual fourni pour chaque
        mention d’auteur référencée un identifiant, un choix entre
        plusieurs identifiants (plusieurs suggestions) ou aucun
        identifiant (avec éventuellement proposition de création
        d’autorité). Un indice de confiance accompagne chaque
        identifiant.

### Prestation d'identification d'autorités par collaboration - équipe/structure commune (*Cas Magellan*)

On cherche à identifier l'ensemble des membres d'une même équipe ou
structure dans une ou plusieurs base d'autorités.

1.  L'utilisateur fournira :

    -   une liste d'appellation des membres d'une équipe
    -   une liste de base d'autorités
    -   une ou plusieurs bases documentaires liées par base d'autorité
    -   informations supplémentaires pour chaque appellation :
        -   date de naissance
        -   id interne
        -   structure
        -   discipline

2.  L'utilisateur souhaite obtenir :

    -   une liste de triplet *\< appellation , id base d'autorités, id
        autorité\>*

3.  Exemple

    Le SCD de l’Université de Lyon 3 s’associe aux laboratoires de
    l’université pour sensibiliser les enseignants-chercheurs à la
    question des identifiants. La première étape est l’attribution d’un
    identifiant IdRef à chaque chercheur membre d’un laboratoire ou
    d’une équipe de recherche. L’appartenance à un même laboratoire ou
    équipe de recherche préfigure une collaboration potentielle et donc,
    des publications communes. Cette information, ainsi que d’autres,
    est exploitée pour établir des liens vers IdRef.

    Le laboratoire fourni une liste de chercheurs, avec pour chacun
    d’eux :

    -   Nom
    -   Prénom
    -   Date de naissance
    -   Laboratoire de rattachement
    -   Établissement de rattachement

    Finalement, SudoQual fourni pour chaque chercheur un identifiant, un
    choix entre plusieurs identifiants (plusieurs suggestions) ou aucun
    identifiant (avec éventuellement proposition de création
    d’autorité). Un indice de confiance accompagne chaque identifiant.

### Alignement de base d'autorités

1.  L'utilisateur fournira :

    -   une liste d'identifiants d'autorité (source),
    -   une ou plusieurs base documentaires liées à ses sources,
    -   une liste d'identifiants d'autorité (cible),
    -   une ou plusieurs base documentaires liées à ses cibles.

2.  L'utilisateur souhaite obtenir :

    -   une liste de couple *\<id autorité source, id autorité cible\>*

3.  Exemples

    1.  Exemple 1

        Persée est une base de publications scientifiques en libre accès
        qui dispose de notices d’autorité internes. Persée s’est
        progressivement associé à l’Abes pour aligner ces notices
        internes aux notices du référentiel IdRef, afin de profiter de
        l’expertise du réseau IdRef et des bénéfices des données liées
        d’IdRef avec d’autres référentiels nationaux et internationaux
        (rebond, génération de nouveaux liens, détection d’anomalies…).
        Depuis, Persée est devenu producteur d’IdRef, c’est-à-dire que
        les documentalistes de Persée ont accès à IdRef en écriture
        (modification et création de notices d’autorité).

        Les notices d’autorités Persée sont fournies avec leurs liens
        aux publications dans la base Persée. On dispose ainsi
        d’autorités enrichies en amount du processus.

        Finalement, SudoQual fournit une liste de couples Persée-IdRef.

    2.  Exemple 2

        Dans l’optique de co-construire un fichier national d’autorités,
        l’Abes et la BnF doivent aligner leurs fichiers d’autorités
        respectifs.

### Recherche de publications

On cherche à récupérer l'ensemble des publications d'une autorité dans
une base documentaire à partir d'un identifiant d'autorité *IdRef* en
utilisant SudoQual.

1.  L'utilisateur fournira :

    -   un identifiant d'autorité *IdRef*
    -   une base documentaire

2.  L'utilisateur souhaite obtenir :

    -   une liste de publications (correspondant aux publications
        rattachées à cette autorité après nettoyage de la base)
    -   \[OPT\] une liste de suggestions ordonnées

3.  Exemple

    A partir de son identifiant IdRef, un chercheur peut rechercher dans
    une ou plusieurs bases de données les publications qu’il a
    produites. Cette recherche s’effectue dans chaque base à partir du
    nom et du prénom du chercheur. Pour chaque publication, les
    métadonnées nécessaires pour l’édition des feature de la RC
    concernée (celle dont le nom et le prénom correspondent au nom,
    prénom de la recherche) sont récupérées pour être fournies en entrée
    à SudoQual. SudoQual compare chaque RC avec la RA représentée par
    l’identifiant du chercheur.

    Finalement, le chercheur récupère une liste de publications, avec
    éventuellement un indice de confiance lui permettant de valider
    manuellement l’attribution.

    Intérêt :

    -   Le chercheur peut facilement recenser ses publications
    -   Le chercheur peut ajouter son identifiant IdRef dans les bases
        de données qui l’autorise
    -   Le chercheur peut –si le système le propose :
        -   soit, se créer un identifiant interne dans la base source et
            y associer ses publications,
        -   soit vérifier que son identifiant interne est bien présent
            dans l’ensemble de ses publications.

# Présentation du framework

*Cette section présente SudoQual de façon succincte afin qu'un
utilisateur* *puisse en comprendre les grandes lignes et l'utiliser.
Pour plus de détails* *sur le fonctionnement de SudoQual veuillez-vous
référer à la section* **.

## Description

SudoQual est un outil permettant de produire des liens entre deux
ensembles de *références d'entité*, un ensemble *source* et un ensemble
*cible*. Ces liens peuvent être de trois types :

-   **sameAs**, la référence d'entité source et la référence d'entité
    cible représentent la même entité ;
-   **suggestedSameAs**, il existe des indices pour dire que la
    référence d'entité source et la référence d'entité cible
    représentent la même entité ;
-   **diffFrom**, la référence d'entité source et la référence d'entité
    cible ne peuvent pas représenter la même entité.

Pour cela SudoQual s'appuie sur des ensembles d'**attributs** (par exemple, pour le cas d'une entité de type personne : nom,
prénom, date de naissance, date de publication, titre, éditeur,
mots-clés…) rattachés à chaque *référence d'entité*. À partir de ces
*attributs*, une configuration de SudoQual (un scénario TODO DEF OU HLIEN) définit un ensemble de
**critères de comparaison** utilisables entre l'ensemble *source* et
l'ensemble *cible*. Ces *critères* sont combinés à travers un **jeu de
règles métiers**. Voici des exemples de règles métiers qu'il est
possible de représenter (++++cas de références d'entité de type personne) :

-   « **SI** la **forme du nom** est proche **ET SI** un
    **co-contributeur** est homonyme **ET SI** un **titre** est assez
    proche, **ALORS** il est ***PROBABLE*** qu’il s’agisse de la même
    personne. »
-   « **SI** les **dates de publication** sont éloignées **ET SI** les
    **contenus** sont éloignées, **ALORS** il est ***TRÈS PROBABLE***
    qu'il ne s'agisse pas de la même personne. » .

C'est à partir de ces *règles métiers* que SudoQual déduit des indices
de co-référence (sameAs) ou de non-co-référence (diffFrom). Ces indices
sont ensuite analysés et combinés afin de produire des liens *sameAs*,
*suggestedSameAs* ou *diffFrom*.

En plus des attributs des références *source* et *cible*, SudoQual
s'appuie également sur un ensemble d'attributs de références dites
**supports**. Cet ensemble *support* sert à enrichir les attributs des
références d'entités *sources* ou *cibles* identifiées comme étant
*sameAs* avec une ou plusieurs références *supports*. Par exemple, il
est possible de construire un ensemble de titres de publications d'un
auteur ou un liste de co-autheurs à partir d'un ensemble de liens
*sameAs* vers des publications connus. Ces liens *sameAs* entre ensemble
*support* et ensemble *source* ou *cible* sont appellés **liens sûrs**.
Les attributs produits à partir de ces *liens sûrs* sont appellés
**attributs calculés**. Les *liens sûrs* peuvent être des liens fournis
en entrée ou des liens *sameAs* précédemment calculés par SudoQual.

Il est important de noter que les ensembles de références *source*,
*cible* et *support* peuvent ne pas être disjoints. Par exemples :

-   l'ensemble *support* peut-être le même que l'ensemble *source*, dans
    ce cas chaque lien *sameAs* calculé par SudoQual entre une référence
    source et une référence cible pourra servir aux calculs d'*attributs
    calculés* de la référence cible liée ;
-   l'ensemble *source* peut-être égal à l'ensemble *cible*, c'est
    notamment le cas si l'on cherche à faire du
    regroupement (*clustering*).

SudoQual est également paramétré par un **mode de liage**, celui-ci
définit des contraintes sur l'ensemble des liens *sameAs* qui peut être
produit. Par défaut les modes suivants sont proposés :

-   **one to one** : *une source* ne peut-être liée qu'à *une seule
    cible* et *une cible* ne peut-être liée qu'à *une seule source* ;
-   **many to one** : *une source* ne peut-être liée qu'à *une seule
    cible* mais *une cible* peut-être liée à *plusieurs sources* ;
-   **many to many** : *une source* peut-être liée à *plusieurs cibles*
    et *une cible* peut-être liée à *plusieurs sources*.

## Quelques configurations notables

### Alignement

On cherche à « aligner » deux ensembles de données distincts
(bijection) :

-   ensemble source ≠ ensemble cibles
-   mode de liage = one to one

### Affectation

On cherche à « affecter » chaque élément de l'ensemble source à un
élément de l'ensemble cible (surjection) :

-   ensemble source ≠ ensemble cibles
-   mode de liage = many to one

### Regroupement

On cherche à repérer des « groupes » dans un ensemble d'élements :

-   ensemble source = ensemble cible
-   mode de liage = many to many

## Architecture générale

SudoQual est composée de plusieurs modules :

-   un module de liage (module principale) ;
-   un moteur de règle (utilisé par le module de liage) ;
-   un module de diagnostic ;
-   un module de clustering.

SudoQual est également composé de plusieurs interfaces utilisateurs :

-   un client lourd (application graphique de bureau) ;
-   une interface en ligne de commandes (utilisation à travers un
    *terminal* — cmd.exe sous Windows) ;
-   des web-services (accessible via des requêtes web — protocol
    « http »).

### Environnement élargi
Pour être utilisé dans un contexte métier, SudoQual doit être intégré dans un environnement logiciel plus large. Il est nécessaire de disposer :
- d'au moins un client pour exploiter les résultats de SudoQual (par exemple, l'Abes dispose de l'interface web paprika.idref.fr);
- d'un module produisant les fichiers d'entrées de SudoQual (à l'Abes, ce module est appelé partition initiale).

La figure suivante montre l'agencement actuel de ces différentes parties. Les
flèches en pointillé représentent des appels optionnels.

![image](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/image-000.png)

## Les différentes fonctionnalités

Cette partie présente les différents *services* (fonctionnalités)
fournis par SudoQual. Ceux-ci sont accessibles à travers les différentes
interfaces (client lourd, cli et web-services). Il se peut cependant que
certains services ne soient pas accessibles à travers toutes les
interfaces.

### link (Liage)

Le service `link` est le service principal de SudoQual. À partir des
données d'entrées, il produit un ensemble de lien *sameAs*,
*suggestedSameAs* ou *diffFrom* entre les références *sources* et les
références *cibles*.

L'entrée et la sortie de ce service sont des structures JSON respectant
les spécifications décrites en section TODO 8.2.3 page 73 (Spécification entrée/sortie).

1.  Exemple d'entrée

    *Note: les exemples ne représentent pas la réalité des attributs ou
    scénarios* *disponibles. Ils ne doivent donc pas être considéré
    comme une spécification* *de ceux-ci.*

    ``` json
    {
      "scenario": "RC-RA",
      "options": {
          "debug": false,
          "validatedSameAsThreshold": 6,
          "suggestedEnabled": false,
      },
      "sources": [
        "sudoc:099407469-1",
        "sudoc:055701159-2",
        "sudoc:118796046-2",
        "sudoc:118585207-1",
        "sudoc:099305364-2",
        "sudoc:099396238-4"
        ],
      "targets": [
        "idref:051626365",
        "idref:076073262"
        ],
      "supports": "sources",
      "safeLinks": [
          {
            "type": "sameAs",
            "source": "sudoc:118796046-2",
            "target": "idref:076073262"
          }
        ],
      "initialLinks": [
          {
            "type": "sameAs",
            "source": "sudoc:118796046-2",
            "target": "idref:076073262"
          },
          {
            "type": "sameAs",
            "source": "sudoc:099407469-1",
            "target": "idref:076073262"
          }
        ],
      "features": {
        "idref:051626365": {
          "firstname": "Rose",
          "lastname": "Dieng",
          "source": ["Méthodes et outils pour la gestion des connaissances 
          \/ Rose Dieng... [et al.]. 2000"],
          "dateCreationNotice": "20000913"
        },
        "idref:076073262": {
          "firstname": "Rose",
          "lastname": "Dieng-Kuntz",
          "birth": "1956",
          "death": "2008",
          "source": ["Construction d\u2019un Web sémantique multi-points de 
          vue \/ par Than Le Bach ; sous la direction de Rose Dieng-Kuntz. 
          Paris, 2006","Représentation et apprentissage de concepts et 
          d'ontologies pour le web sémantique \/ par Alexandre Delteil ; sous 
          la dir. de Rose Dieng-Kuntz. - 2002"],
          "dateCreationNotice": "20040127"
        },
        "sudoc:099407469-1": {
          "firstname": "Rose",
          "lastname": "Dieng",
          "title": "Modeling conflicts in AI : ECAI '96 workshop (W24) (1996)",
          "cocontributors": ["Mueller, H.J."]
        },
        "sudoc:055701159-2": {
          "firstname": "Rose",
          "lastname": "Dieng",
          "title": "Computational conflicts : conflict modeling for 
          distributed intelligent systems, with contributions by numerous 
          experts (2000)",
          "cocontributors": ["Müller, Heinz-Jürgen"]
        },
        "sudoc:118796046-2": {
          "firstname": "Rose",
          "lastname": "Dieng-Kuntz",
          "title": "Construction d’un Web sémantique multi-points de vue (2006)",
          "cocontributors": ["Bach, Than Le"]
        },
        "sudoc:118585207-1": {
          "firstname": "Rose",
          "lastname": "Dieng-Kuntz",
          "title": "ème Conférence \"Terminologie et intelligence artificielle\"
           : actes de la Conférence TIA '07, 8-10 octobre 2007, 
           Sophia-Antipolis, France (2007)",
          "cocontributors": ["Enguehard, Chantal"]
        },
        "sudoc:099305364-2": {
          "firstname": "Rose",
          "lastname": "Dieng-Kuntz",
          "title": "Knowledge management and organizational memories (2002)",
          "cocontributors": ["Matta, Nada"]
        },
        "sudoc:099396238-4": {
          "firstname": "Rose",
          "lastname": "Dieng-Kuntz",
          "title": "Designing cooperative systems : the use of theories and 
          models : proceedings (2000)",
          "cocontributors": [
            "Giboin, Alain", 
            "Karsenti, Laurent", 
            "de Michelis, Giorgio"
            ]
        },
      }
    }
    ```

2.  Exemple de sortie

    ``` json
    {
      "metadata": {
          "version": "sudoqual-v3.0",
          "scenario": "RC-RA",
          "options": {
            "debug": false,
            "validatedSameAsThreshold": 5,
            "suggestedEnabled": false,
          }
      },
      "computedLinks": [
        {
          "type": "sameAs",
          "source": "sudoc:099407469-1",
          "target": "idref:076073262",
          "confidence": 5,
          "why": {}
        },
        {
          "type": "sameAs",
          "source": "sudoc:055701159-2",
          "target": "idref:076073262",
          "confidence": 7,
          "why": {}
        },
        {
          "type": "sameAs",
          "source": "sudoc:118585207-1",
          "target": "idref:076073262",
          "confidence": 5,
          "why": {}
        },
        {
          "type": "suggestedSameAs",
          "source": "sudoc:099305364-2",
          "target": "idref:076073262",
          "confidence": 4,
          "why": {}
        },
        {
          "type": "sameAs",
          "source": "sudoc:099396238-4",
          "target": "idref:076073262",
          "confidence": 6,
          "why": {}
        },
      ]
    }
    ```

### diagnostic

Le service `diagnostic` produit, à partir d'une sortie du service `link`
et de la liste des « lienx initiaux » (liens présents dans la base
documentaire avant l'analyse faite avec SudoQual), un diagnostic des
liens initiaux. Ce service n'est actuellement implémenté que pour le mode
« many-to-one » TODO maj ??.

L'entrée et la sortie de ce service sont des structures JSON respectant
les spécifications décrites en section TODO 3 page 83 (spécifications entrées/sorties). La liste des diagnostics
possibles est décrite en section TODO 7.5 page 63 (Module de diagnostic (many-to-one)).

1.  Exemple d'entrée

    ``` json
    {
      "sources": [
        "sudoc:1", "sudoc:2", "sudoc:3", "sudoc:4"
      ],
      "targets": [
        "idref:a", "idref:b", "idref:c"
      ],
      "initialLinks": [
        {
          "type": "sameAs",
          "source": "sudoc:1",
          "target": "idref:a"
        },
        {
          "type": "sameAs",
          "source": "sudoc:2",
          "target": "idref:b"
        }
      ],
      "computedLinks": [
        {
          "type": "sameAs",
          "source": "sudoc:1",
          "target": "idref:a"
        },
        {
          "type": "sameAs",
          "source": "sudoc:2",
          "target": "idref:a"
        },
        {
          "type": "diffFrom",
          "source": "sudoc:2",
          "target": "idref:b"
        },
        {
          "type": "suggestedSameAs",
          "source": "sudoc:3",
          "target": "idref:a"
        },
      ]
    }
    ```

2.  Exemple de sortie

    ``` json
    {
      "diagnostic": [
        {
          "computedLink": {
            "target": "idref:b"
          },
          "source": "sudoc:1",
          "case": 1,
          "initialLink": "idref:a",
          "status": "validatedLink"
        },
        {
          "source": "sudoc:2",
          "suggestedLinks": [
            {
              "target": "idref:a"
            }
          ],
          "case": 6,
          "initialLink": "idref:a",
          "status": "almostValidatedLink"
        },
        {
          "source": "sudoc:3",
          "suggestedLinks": [
            {
              "target": "idref:c"
            }
          ],
          "case": 12,
          "status": "missingLink"
        },
        {
          "source": "sudoc:4",
          "case": 11,
          "status": "missingLink"
        }
      ]
    }
    ```

### light (link + diagnostic)

Le service `light` est un enchaînement des services `link` et
`diagnostic`. Les spécifications de l'entrée correspondent à celles du
service `link`, celles de la sortie à celles du service `diagnostic`.

1.  Exemple d'entrée

    ``` json
    {
      "scenario": "RC-RA",
      "options": {
          "debug": false,
          "validatedSameAsThreshold": 6,
          "suggestedEnabled": false,
      },
      "sources": [
        "sudoc:099407469-1",
        "sudoc:055701159-2",
        "sudoc:118796046-2",
        ],
      "targets": [
        "idref:051626365",
        "idref:076073262"
        ],
      "supports": "sources",
      "initialLinks": [
          {
            "type": "sameAs",
            "source": "sudoc:118796046-2",
            "target": "idref:076073262"
          },
          {
            "type": "sameAs",
            "source": "sudoc:055701159-2",
            "target": "idref:051626365"
          },
          {
            "type": "sameAs",
            "source": "sudoc:099407469-1",
            "target": "idref:051626365"
          }
        ],
      "safeLinks": [
          {
            "type": "sameAs",
            "source": "sudoc:118796046-2",
            "target": "idref:076073262"
          }
        ],
      "features": {
        "idref:051626365": {
          "firstname": "Rose",
          "lastname": "Dieng",
          "source": ["Méthodes et outils pour la gestion des connaissances 
          \/ Rose Dieng... [et al.]. 2000"],
          "dateCreationNotice": "20000913"
        },
        "idref:076073262": {
          "firstname": "Rose",
          "lastname": "Dieng-Kuntz",
          "birth": "1956",
          "death": "2008",
          "source": ["Construction d\u2019un Web sémantique multi-points de 
          vue \/ par Than Le Bach ; sous la direction de Rose Dieng-Kuntz. 
          Paris, 2006","Représentation et apprentissage de concepts et 
          d'ontologies pour le web sémantique \/ par Alexandre Delteil ; sous 
          la dir. de Rose Dieng-Kuntz. - 2002"],
          "dateCreationNotice": "20040127"
        },
        "sudoc:099407469-1": {
          "firstname": "Rose",
          "lastname": "Dieng",
          "title": "Modeling conflicts in AI : ECAI '96 workshop (W24) (1996)",
          "cocontributors": ["Mueller, H.J."]
        },
        "sudoc:055701159-2": {
          "firstname": "Rose",
          "lastname": "Dieng",
          "title": "Computational conflicts : conflict modeling for 
          distributed intelligent systems, with contributions by numerous 
          experts (2000)",
          "cocontributors": ["Müller, Heinz-Jürgen"]
        },
        "sudoc:118796046-2": {
          "firstname": "Rose",
          "lastname": "Dieng-Kuntz",
          "title": "Construction d’un Web sémantique multi-points de vue (2006)",
          "cocontributors": ["Bach, Than Le"]
        }
      }
    }
    ```

2.  Exemple de sortie

    idem TODO 2 page 17 (sortie diagnostic)

### cluster (Regroupement)

Le service `cluster` est un enchaînement du service `link` (en mode
*many-to-many* et avec l'ensemble *source* égal à l'ensemble *cible*) et
du module de clustering. Il produit une liste de clusters (regroupement
de références).

L'entrée de ce service correspond à l'entrée du service `link`. La
sortie est une structure JSON respectant les spécifications décrites en
section 2 page 86. (Spécification des entrées/sorties).

1.  Exemple d'entrée

    ``` json
    {
      "features": {
        "http://www.sudoc.abes.fr/011522968-1": {
          "title": [
            "Environnement de développement d'applications utilisant le modèle des GC"
          ],
          "pubDate": "1994"
        },
        "http://www.sudoc.abes.fr/01235127X-2": {
          "title": [
            "La Manipulation directe en interface homme-machine"
          ],
          "pubDate": "1990"
        },
        "http://www.sudoc.abes.fr/155198777-2": {
          "title": [
            "Conceptual Structures: Theory, Tools and Applications"
          ],
          "pubDate": "2006"
        },
        "http://www.sudoc.abes.fr/145888193-1": {
          "title": [
            "Etude des décompositions d'un réseau"
          ],
          "pubDate": "1967"
        },
      },
      "sources": [
        "http://www.sudoc.abes.fr/011522968-1",
        "http://www.sudoc.abes.fr/01235127X-2",
        "http://www.sudoc.abes.fr/155198777-2",
        "http://www.sudoc.abes.fr/145888193-1",
      ],
      "scenario": "sudoc-rc-rc",
      "targets": "sources"
    }
    ```

2.  Exemple de sortie

    ``` json
    {
      "clusters": [
        {
          "source": "http://www.sudoc.abes.fr/011522968-1",
          "type": "sameAs",
          "target": "_:cluster1"
        },
        {
          "source": "http://www.sudoc.abes.fr/01235127X-2",
          "type": "sameAs",
          "target": "_:cluster1"
        },
        {
          "source": "http://www.sudoc.abes.fr/155198777-2",
          "type": "sameAs",
          "target": "_:cluster2"
        },
        {
          "source": "http://www.sudoc.abes.fr/145888193-1",
          "type": "sameAs",
          "target": "_:cluster2"
        }
      ]
    }
    ```

### complete (light+cluster+rara)

Le service `complete` est une combinaison de plusieurs exécutions du
service `link` (sources/sources, cibles/cibles et sources/cibles) suivie
par l'exécution de plusieurs modules spécifiques. Cette combinaison a
été pensée dans le but de produire un rapport **complet** pour un
ensemble de sources et de cibles données.

L'entrée et la sortie de ce service sont des structures JSON respectant
les spécifications décrites en section 1 page 97 (spécifications entrées/sorties).

1.  Exemple d'entrée

    ``` json
    {
      "features": {
        "sudoc:152991425-1": {
          "role": [
            "author"
          ],
          "title": [
            "Aloïse Corbaz, \"Cloisonné de théâtre\""
          ],
          "pubDate": "2011",
          "personName": [
            {
              "appellation": "Boulanger, Christophe"
            }
          ],
          ...
      },
      "initialLinks": [
        {
          "source": "sudoc:190495286-1",
          "type": "sameAs",
          "target": "idref:108780600"
        },
        ...
      ],
      "sources": [
        "sudoc:235360856-12",
        ...
        "sudoc:043733492-1"
      ],
      "targets": [
        "idref:108780600",
        "idref:135658764",
        "idref:155920693",
        "idref:08168424X",
        "idref:108118444"
      ],
      "sources-sources": {
        "scenario": "sudoc-rc-rc"
      },
      "sources-targets": {
        "scenario": "sudoc-rc-ra",
        "supports": "sources"
      },
      "targets-targets": {
        "scenario": "sudoc-ra-ra"
      }
    }
    ```

2.  Exemple de sortie

    ``` json
    {
       "inconsistencies": [
        [ 
         {
            "source": "sudoc:129691305-1",
            "type": "diffFrom",
            "target": "idref:08168424X"
         },
         {
            "source": "sudoc:129691305-1",
            "type": "sameAs",
            "target": "idref:08168424X"
         }
        ],
        ...
       ],
       "diagnosticSources": [
         {
            "computedLink": {
               "why": {},
               "target": "idref:155920693"
            },
            "source": "sudoc:235360856-12",
            "suggestedLinks": [],
            "case": 9,
            "status": "missingLink"
         },
         ...
       ],
       "metadata": {
         "version-framework": "2.9.2",
         "service": "complete",
         "version-scenario": "1.13.1"
       },
       "computedLinks-sources-targets": [
         {
            "confidence": "always",
            "why": {"link": {}},
            "step": 1,
            "source": "sudoc:129691305-1",
            "type": "diffFrom",
            "target": "idref:08168424X"
         },
         ...
       ],
       "computedLinks-sources-sources": [
         {
            "confidence": 6,
            "why": {"link": {}},
            "step": 1,
            "source": "sudoc:16613094X-3",
            "type": "sameAs",
            "target": "sudoc:101507410-4"
         },
         ...
       ],
       "computedLinks-targets-targets": [{
         "type": "sameAs",
         "targets": [
            "idref:155920693",
            "idref:135658764"
         ]
       }],
       "clusters": [
         {
            "cluster": "_:cluster1",
            "source": "sudoc:196014735-4",
            "type": "sameAs"
         },
         {
            "cluster": "_:cluster1",
            "source": "sudoc:196012953-4",
            "type": "sameAs"
         },
         ...
       ]
    }
    ```

### Alignement de référentiels d'autorités (align)

Le service `align` est prévu pour identifier les co-références entre
deux référentiels d'autorités.

L'entrée et la sortie de ce service sont des structures JSON respectant
les spécifications décrites en section TODO 8.4.2 page 105.

1.  Exemple d'entrée

    ``` json
    {
        "scenario-ra-ra": "sudoc-sra-sra",
        "scenario-rc-rc": "sudoc-rc-rc",
        "sources": [
            "idhal:1",
            "idhal:2"
        ],
        "targets": [
            "idref:1",
            "idref:2"
        ],
        "supports": [
            "hal:1",
            "sudoc:1",
            "hal:2",
            "sudoc:2"
        ],
        "initialLinks" : [
            {"source":"idhal:1","type":"sameAs","target":"hal:1"},
            {"source":"idhal:2","type":"sameAs","target":"hal:2"},
            {"source":"idref:1","type":"sameAs","target":"sudoc:1"},
            {"source":"idref:2","type":"sameAs","target":"sudoc:2"}
        ],
        "features": {
            "idhal:1": {
                "personName": ["dupont, jean-marie"]
            },
            "idhal:2": {
                "personName": ["dupont, jean-marie"]
            },
            "idref:1": {
                "personName": ["dupont, jean-marie"]
            },
            "idref:2": {
                "personName": ["dupont, jean-marie"]
            },
            "hal:1": {
                "title": ["Ceci est un super titre"],
                "corporateBody": ["Abes"]
            },
            "hal:2": {
                "cocontributor": ["Chirac, Jacques"]
            },
            "sudoc:1": {
                "title": ["Ceci est un super titre"],
                "corporateBody": ["Abes"]
            },
            "sudoc:2": {
                "cocontributor": ["Chirac, Jacques"]
            }
        }
    }
    ```

2.  Exemple de sortie

    ``` json
    {
      "computedLinks": [
        {
          "confidence": 6,
          "why": {},
          "step": 1,
          "source": "idhal:1",
          "type": "sameAs",
          "target": "idref:1"
        },
        {
          "confidence": 6,
          "why": {},
          "step": 1,
          "source": "idhal:2",
          "type": "sameAs",
          "target": "idref:2"
        }
      ],
      "metadata": {
        "version-framework": "2.9.2",
        "service": "align",
        "version-scenario": "1.13.1"
      },
      "clusters": []
    }
    ```

### Évaluation d'un fichier de benchmark (eval)

La fonction « évaluation » produit, à partir des données d'entrées et
d'une liste de liens attendus, un rapport de qualité concernant les
liens produits par le mode « link » de SudoQual. Actuellement, la
fonction *eval* n'est disponible que pour le mode de liage *many-to-one*
mais pourrait être pensée pour les autres modes.

Pour chaque référence appartenant à l'ensemble *source*, les liens
produits sont évalués et classés dans un des quatres cas suivants :

-   good
-   careful
-   unsatisfactory
-   bad

Pour plus d'information sur ce mode, référez-vous à la section 7.6 page 6 (Évaluation (benchmark)).

1.  Exemple d'entrée

    ``` json
    {
      "input": {
        "features": {
          "sudoc:1": {},
          "sudoc:2": {},
          "idref:a": {},
          "idref:b": {}
        },
        "sources": [
          "sudoc:1",
          "sudoc:2"
        ],
        "scenario": "sudoc-rc-ra",
        "supports": "sources",
        "targets": [
          "idref:a",
          "idref:b"
        ]
      },
      "expectedLinks": [
        {
          "source": "sudoc:1",
          "type": "sameAs",
          "target": "idref:a"
        },
        {
          "source": "sudoc:2",
          "type": "sameAs",
          "target": "idref:b"
        }
      ]
    }
    ```

2.  Exemple de sortie

    ``` text
              good: 1
           careful: 0
    unsatisfactory: 0
               bad: 1
    ```

### Comparaison (compare)

La fonction « compare » produit, à partir de deux sorties de SudoQual,
une liste des différences entre les liens de la première sortie et les
liens de la deuxième. Cette fonction est pensée afin de pouvoir étudier
l'impact des modifications apportées à SudoQual ou sa configuration (par exemple sur un scénario : modification d'un critère, d'un attribut, d'une règle...).

La sortie de cette fonction n'est actuellement pas spécifié. Voici un
exemple de sortie de la version actuelle.

``` text
expected link http://www.sudoc.abes.fr/102222142-3 sameAs
    http://www.idref.fr/068944470/id (step  4)  not found, 
computed link http://www.sudoc.abes.fr/137325096-6 suggestedSameAs 
    http://www.idref.fr/195722027/id (step  5) not expected,
http://www.sudoc.abes.fr/021186707-1 diffFrom 
    http://www.idref.fr/160464633/id  - steps does not match: expected 2, found 3
```

### Listing des scénarios disponible

# Utilisation du CLI

*Le client Batch ou CLI (Command Line Interface) est un client SudoQual
exécutable depuis un terminal Unix ou Windows. Sur Windows l'utilisation
de *cmd.exe* est conseillée.*

## Liage

Le mode de liage du CLI s'active en spécifiant `link` en premier
paramètre de la ligne de commande, puis il faut lui passer un fichier
d'entrée conforme aux spécifications du module de liage (cf. [Spécification entrée/sortie : section 1](#sp%C3%A9cification-entr%C3%A9esortie) ).

Ce fichier d'entrée peut être passé soit sur l'entrée standard, soit par
l'option `--input` :

-   `$ java -jar sudoqual-cli.jar link < input-file.json`
-   `$ java -jar sudoqual-cli.jar link --input input-file.json`

Le CLI écrira le résultat de l'exécution en JSON sur la sortie standard.
Vous pouvez rediriger la sortie standard dans un fichier de la manière
suivante :

``` shell
$ java -jar sudoqual-cli.jar link < input-file.json > output-file.json
```

L'option `--nb-thread` permet de forcer le nombre de *threads*
(processus en parallèles) utilisés. Par défaut, le code Java suivant
`Runtime.getRuntime().availableProcessors()` est utilisé pour
**estimer** le nombre de processeurs disponibles, un *thread* de
traitement sera alors créer par processeur disponibles.

## Diagnostic *ManyToOne*

Vous pouvez utiliser le CLI pour comparer deux ensembles de liens,
généralement un ensemble de liens initiaux et un ensemble de liens
calculés. Pour cela, il faut spécifier `diagnostic` en premier paramètre
de la ligne de commande:

``` shell
$ java -jar sudoqual-cli.jar diagnostic < input-file.json
```

Le fichier `input-file.json` doit contenir quatre entrées de premier
niveau :

-   `sources` et `targets` : l'ensemble des sources et des cibles à
    évaluer en tant que tableau de chaînes de caractères ;
-   `initialLinks` et `computedLinks` : l'ensemble des liens initiaux et
    calculés en tant que tableau d'objet JSON contenant une entrée
    `source`, une entrée `target` et un entrée `type` valant soit
    `sameAs` soit `diffFrom`.

## Liage + diagnostic

Vous pouvez déclencher le diagnostic *« many to one »* après l'exécution
du module de liage. Pour cela, utilisez l'option `--diagnostic` de la
commande `link`.

Les spécifications du fichier d'entrée sont ceux du mode « liage » (cf.
section TODO 8.2.3 page 73 Spécification entrée/sortie), l'entrée de premier niveau `initialLinks` devient ici
obligatoire.

``` shell
$ java -jar sudoqual-cli.jar link --diagnostic < input-file.json
```

## Liage + clustering

Vous pouvez également déclencher le clustering après l'exécution du
module de liage. Pour cela, utilisez l'option `--clustering` de la
commande `link`.

Les spécifications du fichier d'entrée sont ceux du mode « liage » (cf.
section TODO 1 page 73 Spécification entrée/sortie), le scénario utilisé doit être de type *many-to-many* et
l'ensemble *source* doit être le même que l'ensemble *cible* (targets =
sources).

``` shell
$ java -jar sudoqual-cli.jar link --clustering < input-file.json
```

## Comparaison de deux fichiers de sortie

Vous pouvez également demander au CLI de comparer deux fichiers de
sortie (du mode liage) en spécifiant `compare` en premier paramètre de
la ligne de commande:

``` shell
$ java -jar sudoqual-cli.jar compare actual.json expected.json
```

Exemple de sortie :

``` shell
sudoc:234882352-1 sameAs idref:23488035X  - steps does not match: expected 1, found 2
expected link sudoc:234881828-2 suggestedSameAs idref:23488035X (step  2)  not found,
computed link sudoc:234881828-2 sameAs idref:23488035X (step  2) not expected,
```

## Liage + comparaison

En mode liage, au lieu de récupérer la sortie calculée, vous pouvez
demander de la comparer avec une sortie attendue en utilisant l'option
`--compare-with` :

``` shell
$ java -jar sudoqual-cli.jar link --compare-with expected.json < input-file.json
```

## Evaluation d'un fichier de benchmark

Le CLI permet également d'évaluer un fichier de benchmark via la command
`eval` spécifiée en premier paramètre de la ligne de commande. Le
fichier passé sur l'entrée standard ou via l'option `--input` doit
correspondre aux spécifications présentées en section TODO 7.6 page 65 (Évaluation (benchmark)).

``` shell
$ java -jar sudoqual-cli.jar eval < bench-file.json
```

## Autres options

### Pretty Print

Vous pouvez désactiver le formatage des sorties JSON avec l'option
`--no-pretty-print`. Cela permet d'obtenir des fichiers de sortie plus
compact (mais moins lisible).

### Log Level

L'option `--log-level` permet de configurer le niveau de *log* à partir
duquel les messages sont écrit sur la *sortie d'erreurs standard*. Les
différentes valeurs possibles sont :

-   OFF
-   TRACE
-   DEBUG
-   INFO
-   WARN
-   ERROR
-   ALL

C'est un système par niveau donc choisir le niveau `INFO` affichera
également les niveaux `WARN` et `ERROR` mais pas les niveaux `TRACE` et
`DEBUG`. Les niveaux `ALL` et `OFF` sont deux niveaux particuliers, pour
plus d'information voir <https://logback.qos.ch/manual/>.

### Encodage des caractères des fichiers d'entrées

L'option `--charset` permet de spécifier l'encodage des caractères
utilisés par les fichiers d'entrées. Par défaut, l'encodage utilisé est
`UTF-8`.

### Version du CLI

Pour vérifier la version du CLI installé sur votre ordinateur utiliser
l'option `--version`.

``` shell
$ java -jar sudoqual-cli.jar --version
=====================================
=         SudoqualCommander         =
=====================================
Version: 1.5.3
Built on: 2019-06-18T20:37:02Z
Produced by: Clément SIPIETER
```

### Help

En cas de doute sur les options à utiliser, pensez à l'option `--help`.

``` shell
$ java -jar sudoqual-cli.jar --help
=====================================
=         SudoqualCommander         =
=====================================

java -jar sudoqual-cli.jar [main-options] command [command-options]
Usage:  [options] [command] [command options]
  Options:
    --charset
      Specify charset to use for read input files
    -h, --help
      Print this message
    --log-level
      Set the logging level. Possible values: TRACE, DEBUG, INFO, WARN, ERROR, 
      ALL and OFF.
      Default: WARN
    --no-pretty-print
      Disable pretty print.
      Default: true
    --scenario-dir
      Specify directory path where find scenario files.
    -v, --verbose
      Enable verbose mode
      Default: false
    -V, --version
      Print version information
  Commands:
    link      run link
      Usage: link [options]
        Options:
          -c, --clustering
            Enable clustering (available only for many-to-many with 
            targets==sources configuration)
            Default: false
          --compare-with
            Compare the output with the given other output (a json file)
          -d, --diagnostic
            Enable diagnostic (available only for many-to-one configuration)
            Default: false
          -f, --input
            Input file in JSON
            Default: -
          --nb-threads
            Number of threads to use.
            Default: 4

    compare      Compare two output files
      Usage: compare <JSON actual file> <JSON expected file>

    eval      Run evaluation
      Usage: eval <JSON bench file> [<JSON bench file>...]

    diagnostic      run diagnostic
      Usage: diagnostic [options]
        Options:
          -f, --input
            Input file in JSON
            Default: -
```

# Utilisation des web services

Ce module permet de lancer le « module de liage » (service **link**), le
« module de diagnostic » (service **diagnostic**), un enchaînement des
deux services « module de liage » puis « module de diagnostic » (service
**complete**) ou encore un enchaînement du service « module de liage »
et du « module de clustering » (service **cluster**). Ceci à travers des
requêtes HTTP afin d'en faciliter l'utilisation par des interfaces Web
tel que « Paprika ». *D'autres services pourront s'ajouter par la suite
tel que des services de diagnostics spécifiques ou des « workflow »
intégrés* *(enchaînement de plusieurs services) en fonction de
l'évolution des besoins et des configurations du module de liage
disponibles.*

Par défaut, ces web services sont « stateless », par exemple un appel à
**link** déclenche une exécution du module de liage. Une fois le
résultat disponible, celui-ci est accessible par une requête HTTP de
type GET à l'adresse "/results/\<jobId\>" (le « jobId » est transmis au
client lors de l'appel au service **link**), les données ayant servi au
calcul de celui-ci sont accessible de la même manière à l'adresse
"/inputs/\<jobId\>" ainsi que le service qui à produit ce résultat
l'adresse "/services/\<jobId\>". Une fois le résultat d'un « job »
calculé aucune donnée intermédiaire n'est conservé sur le serveur.

Cependant, pour des questions d'optimisation des performances du service
**link**, il est possible de demander au serveur de mettre en cache le
résultat de l'évaluation des *filtres* et *critères* pour une
réutilisation ultérieur (cf. section 5 page 36 - Utilisation du mode interactif) . Cela sera fait en affectant la
valeur `true` à l'option `interactif`.

### Utilisation générale des web services

*Ces informations sont valables pour les services **link**,
**diagnostic** et **complete**.*

*Note: les crochets "\[\]" indique quelque chose d'optionnel, les
chevrons "\<\>" indique un paramètre. Les entêtes HTTP doivent être
conforme à la rfc2616* *<https://tools.ietf.org/html/rfc2616>.*

1.  Lancer une exécution

    1.  Requête

        -   Adresse :
            <http://>\<adresse_du_serveur\>\[/\<chemin_racine_de_l\_application\>\]/\<nom_du_service\>
        -   Méthode : POST
        -   contenu : données d'entrée au format JSON, voir
            spécification du service.

        ``` text
        POST [/<chemin_racine_de_l_application>]/<nom_du_service>
        Host: <adresse_du_serveur>
        Accept: application/json
        Content-Type: application/json;charset=UTF-8
        Content-Length: <taille_de_la_partie_contenu_en_octet>

        <input_json>
        ```

    2.  Réponses possibles

        -   **202**: La demande d'exécution à bien été prise en compte

            Le serveur fournit, via le header "Location", l'adresse à
            laquelle le client peut récupérer les informations sur
            l'exécution en cours.

            ``` text
            HTTP/1.1 202
            Location: /jobs/<jobId>
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Length: 0
            ```

        -   **303**: Le résultat de l'exécution demandée existe déjà

            Le serveur redirige le client vers le résultat de
            l'exécution.

            ``` text
            HTTP/1.1 303
            Location: /results/<jobId>
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Length: 0
            ```

        -   **400** - La syntaxe de la requête est erronée

            ``` text
            HTTP/1.1 400
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Type: application/json;charset=UTF-8
            Content-Length: 108

            {
                "error": "<error_type>",
                "detail": "<error_message>"
            }
            ```

            exemple:

            ``` text
            HTTP/1.1 400
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Type: application/json;charset=UTF-8
            Content-Length: 329

            {
                "error": "Json input does not fulfill requirements.",
                "detail": "#: 6 schema violations found\n#: required key [scenario] not 
                found\n#: required key [sources]   not found\n#: required key [targets] 
                not found\n#: required key [features] not found\n#: required key 
                [initialLinks] not found\n#: extraneous key [toto] is not permitted\n"
            }
            ```

        -   **500**: Défaillance du serveur

            Cette erreur indique un problème du serveur, cela ne devrait
            pas se produire. Merci de signaler toute erreur de ce type.

            ``` text
            HTTP/1.1 500 
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Type: application/json;charset=UTF-8
            Content-Length: 68

            {
                "error": "<error_type>",
                "detail": "<error_message>"
            }
            ```

2.  Connaître l'état d'une exécution

    1.  Requête

        -   Adresse :
            <http://>\<adresse_du_serveur\>\[/\<chemin_racine_de_l\_application\>\]/jobs/\<jobId\>
        -   Méthode : GET

        ``` text
        GET [/<chemin_racine_de_l_application>]/jobs/<jobId>
        Host: <adresse_du_serveur>
        Accept: application/json
        Content-Length: 0
        ```

    2.  Réponses possibles

        -   **200**: Le job n'est pas encore terminé ou une erreur s'est
            produite durant l'exécution

            ``` text
            HTTP/1.1 200
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Type: application/json;charset=UTF-8
            Content-Length: 24

            {
                "status": "IN_PROGRESS"
            }
            ```

            Le status peut-être: PENDING, IN_PROGRESS OU FAIL. Le status
            doit donc être lu et vérifié à chaque appel afin d'avertir
            l'utilisateur final d'un possible échec.

        -   **303**: Le job s'est terminé avec succès

            Le résultat de l'exécution est disponible à l'adresse :
            <http://>\<adresse_du_serveur\>\[/\<chemin_racine_de_l\_application\>\]/results/\<jobId\>.
            Le serveur renvoie une redirection vers cette adresse.

            ``` text
            HTTP/1.1 303 
            Content-Length: 0
            Date: <date>
            Location: [/<chemin_racine_de_l_application>]/results/<jobId>
            Server: SudoQual WS/0.1.0
            ```

        -   **404**: Le job demandé n'existe pas ou n'existe plus

            ``` text
            HTTP/1.1 404
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Length: 0
            ```

            Pour un job ayant été annulé le serveur peut également
            renvoyer la réponse suivante :

            ``` text
            HTTP/1.1 200
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Type: application/json;charset=UTF-8
            Content-Length: 22

            {
                "status": "CANCELLED"
            }
            ```

        -   **400**: La syntaxe de la requête est erronée

            ``` text
            HTTP/1.1 400
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Type: application/json;charset=UTF-8
            Content-Length: 108

            {
                "error": "<error_type>",
                "detail": "<error_message>"
            }
            ```

            exemple :

            ``` text
            HTTP/1.1 400
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Type: application/json;charset=UTF-8
            Content-Length: 77

            {
                "error": "The jobId is not a valid id",
                "detail": "For input string: \"toto\""
            }
            ```

        -   **500**: Défaillance du serveur

            Cette erreur indique un problème du serveur, cela ne devrait
            pas se produire. Merci de signaler toute erreur de ce type.

            ``` text
            HTTP/1.1 500 
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Type: application/json;charset=UTF-8
            Content-Length: 68

            {
                "error": "<error_type>",
                "detail": "<error_message>"
            }
            ```

3.  Arrêter une exécution ou supprimer les résultats d'une exécution

    1.  Requête

        -   Adresse :
            <http://>\<adresse_du_serveur\>\[/\<chemin_racine_de_l\_application\>\]/jobs/\<jobId\>
        -   Méthode : DELETE

        ``` text
        DELETE [/<chemin_racine_de_l_application>]/jobs/<jobId>
        Host: <adresse_du_serveur>
        Content-Length: 0
        ```

    2.  Réponses possibles

        -   **202**: La demande de suppression à bien été prise en
            compte

            ``` text
            HTTP/1.1 202
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Length: 0
            ```

        -   **400**: La syntaxe de la requête est erronée

            ``` text
            HTTP/1.1 400
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Type: application/json;charset=UTF-8
            Content-Length: 108

            {
                "error": "<error_type>",
                "detail": "<error_message>"
            }
            ```

        -   **404**: La ressource à supprimer n'existe pas

            ``` text
            HTTP/1.1 404
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Length: 0
            ```

        -   **500**: Défaillance du serveur

            Cette erreur indique un problème du serveur, cela ne devrait
            pas se produire. Merci de signaler toute erreur de ce type.

            ``` text
            HTTP/1.1 500 
            Server: SudoQual WS/<serveur_version>
            Date: <date>
            Content-Type: application/json;charset=UTF-8
            Content-Length: 68

            {
                "error": "<error_type>",
                "detail": "<error_message>"
            }
            ```

4.  Récupération des entrées/sorties d'une exécution

    Une fois un job terminé, son résultat mais aussi son entrée ainsi
    que le service utilisé pour le calcul sont accessibles durant une
    durée définie dans le fichier de configuration du serveur. Une fois
    cette durée écoulée, toutes les informations concernant ce job sont
    supprimées du serveur.

    Si vous demander les informations d'un job qui n'existe pas ou plus,
    le serveur renverra un code d'erreur HTTP 404. Une erreur de type
    500 est toujours possible et indique un problème du serveur, cela ne
    devrait pas se produire, merci de signaler toute erreur de ce type.

    1.  Requête pour obtenir le résultat de l'exécution d'un job

        -   Adresse :
            <http://>\<adresse_du_serveur\>\[/\<chemin_racine_de_l\_application\>\]/results/\<jobId\>
        -   Méthode : GET

        Format de la réponse :

        ``` text
        HTTP/1.1 200
        Content-Type: application/json;charset=UTF-8
        Date: <date>
        Content-Length: <content-length>

        <result_as_json>
        ```

    2.  Requête pour obtenir l'input JSON d'un job

        -   Adresse :
            <http://>\<adresse_du_serveur\>\[/\<chemin_racine_de_l\_application\>\]/inputs/\<jobId\>
        -   Méthode : GET

        Format de la réponse :

        ``` text
        HTTP/1.1 200
        Content-Type: application/json;charset=UTF-8
        Date: <date>
        Content-Length: <content-length>

        <input_json>
        ```

    3.  Requête pour obtenir le service associé à un job

        -   Adresse :
            <http://>\<adresse_du_serveur\>\[/\<chemin_racine_de_l\_application\>\]/services/\<jobId\>
        -   Méthode : GET

        Format de la réponse :

        ``` text
        HTTP/1.1 200
        Content-Type: application/json;charset=UTF-8
        Date: <date>
        Content-Length: <content-length>

        {
            "service": "<nom_du_service>"
        }
        ```

### Détails des web services

1.  Web service link

    -   Adresse :
        <http://>\<adresse_du_serveur\>\[/\<chemin_racine_de_l\_application\>\]/link
    -   Méthode : POST
    -   contenu : données d'entrée en JSON

    Les entrées/sorties correspondent à ceux du module de liage (cf.
    section TODO 8.2.3 page 73), la partie contenu de la requête HTTP doit respecter le
    format et les contraintes de ce module. Toutefois, l' *option*
    supplémentaire suivante est disponible :

    -   **interactif** de type *booléenne*: demande au serveur d'activer
        le mode interactif, voir

    ``` text
    POST [/<chemin_racine_de_l_application>]/link
    Host: <adresse_du_serveur>
    Content-Type: application/json
    Content-Length: <size_of_the_content_part_in_byte>

    {json content}
    ```

2.  Web service diagnostic

    -   Adresse :
        <http://>\<adresse_du_serveur\>\[/\<chemin_racine_de_l\_application\>\]/diagnostic
    -   Méthode : POST
    -   contenu : données d'entrée en JSON

    Les entrées/sorties correspondent à ceux du module de diagnostic
    (voir section TODO 3 page 83), la partie contenu de la requête HTTP doit respecter
    le format et les contraintes de ce module.

    ``` text
    POST [/<chemin_racine_de_l_application>]/diagnostic
    Host: <adresse_du_serveur>
    Content-Type: application/json
    Content-Length: <size_of_the_content_part_in_byte>

    {json content}
    ```

3.  Web service complete

    Le web service complete est une combinaison du web service "link" et
    du web service "diagnostic".

    -   Adresse :
        <http://>\<adresse_du_serveur\>\[/\<chemin_racine_de_l\_application\>\]/complete
    -   Méthode : POST
    -   contenu : données d'entrée en JSON

    L'entrée correspond à l'entrée du module de liage (cf. section TODO) où
    la propriété "initialLinks" devient obligatoire.

    L'*option* supplémentaire suivante est disponible :

    -   **interactif** de type *booléenne*: demande au serveur d'activer
        le mode interactif, voir TODO

    ``` text
    POST [/<chemin_racine_de_l_application>]/complete
    Host: <adresse_du_serveur>
    Content-Type: application/json
    Content-Length: <size_of_the_content_part_in_byte>

    {json content}
    ```

4.  Web service cluster

    -   Adresse :
        <http://>\<adresse_du_serveur\>\[/\<chemin_racine_de_l\_application\>\]/cluster
    -   Méthode : POST
    -   contenu : données d'entrée en JSON

    Les entrées/sorties correspondent à ceux du module de clustering
    (voir section ), la partie contenu de la requête HTTP doit respecter
    le format et les contraintes de ce module.

    ``` text
    POST [/<chemin_racine_de_l_application>]/cluster
    Host: <adresse_du_serveur>
    Content-Type: application/json
    Content-Length: <size_of_the_content_part_in_byte>

    {json content}
    ```

5.  Utilisation du mode interactif

    Si un *job* de type *link* ou *complete* a été lancé avec l'option
    `interactif` à `true`, vous pouvez alors le réutiliser en envoyant
    un contenu JSON de la forme suivante à l'adresse du service
    correspondant (cf. *link* ou *complete*):

    -   **interactif**: objet JSON
        -   **jobId**: correspond à l'id du job à réutiliser
        -   **newSafeLinks**: un ensemble de liens sûrs à ajouter pour
            continuer le calcul précédent.

    1.  Spécification du format d'entrée

        ``` json
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "title": "link service: interactive mode",
          "type": "object",
          "additionalProperties": false,
          "required": [
            "interactif"
          ],
          "properties": {
            "interactif" : {
              "type": "object",
              "additionalProperties": false,
              "required": [
                  "jobId",
                  "newSafeLinks",
              ],
              "properties": {
                "jobId": { 
                    "type": "string" 
                  }, 
                  "newSafeLinks": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "type": {
                        "type": "string"
                      },
                      "source": {
                        "type": "string"
                      },
                      "target": {
                        "type": "string"
                      }
                    },
                    "required": [
                      "type",
                      "source",
                      "target"
                    ],
                    "additionalProperties": false
                  }
                }
              }
            }
          }
        }
        ```

    2.  Exemple d'entrée

        ``` json
        {
           "interactif": {
              "jobId": "b49a34ca", 
              "newSafeLinks": [
                {
                  "source": "http://www.sudoc.abes.fr/089072642-1",
                  "target": "http://www.idref.fr/129493112/id",
                  "type": "sameAs"
                },
                {
                  "source": "http://www.sudoc.abes.fr/087032321-2",
                  "target": "http://www.idref.fr/129493112/id",
                  "type": "diffFrom"
                }
              ]
           }
        }
        ```

    3.  Sortie

        La sortie correspond à la sortie du service appelé (*link* ou
        *complete*). Toutefois, la sortie peut contenir des informations
        supplémentaires utilisées pour le bon fonctionnement du mode
        interactif.

### Web service info

Ce service supplémentaire permet d'obtenir des informations sur le
serveur. Notamment la version de celui-ci et les scénarios disponibles
pour le web service link.

1.  Requête

    -   Adresse : <http://>\<adresse_du_serveur\>/sudoqual/info
    -   Méthode : GET

    ``` text
    GET /sudoqual/info
    Host: <adresse_du_serveur>
    ```

2.  Réponses possibles

    1.  200 OK

        L'exécution du programme s'est déroulée normalement. La partie
        **contenu** de la réponse HTTP est au format JSON et respecte la
        syntaxe suivante :

        ``` json
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "title": "info web service result",
          "type": "object",
          "additionalProperties": true,
          "required": [
            "version",
            "scenarios"
          ],
          "properties": {
            "version": { "type": "string" },
            "scenarios": {
              "type" : "array",
              "items" : { "type": "string"}
            }
          }
        }
        ```

        Exemple de contenu possible :

        ``` json
        {
          "version" : "SudoQualWS/1.0.0",
          "scenarios": [
            "RC-RA",
            "RA-RA",
            "RC-RC"
          ]
        }
        ```

        Il est à noter que la spécification du contenu de la réponse
        autorise la présence de propriétés supplémentaires. Ce web
        service pourra donc à terme fournir d'autre informations.

    2.  Erreur 500 - Défaillance du serveur

        Cette erreur indique un problème du serveur, cela ne devrait pas
        se produire. Merci de signaler toute erreur de ce type.

# Utilisation du client lourd

Le client lourd se présente sous la forme d'un « plugin » pour
l'application Eclipse. Il permet l'exécution, le paramétrage et le
débogage des configurations du framework SudoQual.

## Installation

### Pré-requis

-   **Java 11 ou supérieur** (*Testé avec Java 12*)
-   **Eclipse 4 ou supérieur**
    -   installer Eclipse :
        1.  aller sur <https://www.eclipse.org/downloads/>
        2.  cliquer sur Télécharger
        3.  Lors de l'installation sélectionner « Eclipse IDE for Java
            Developers »
    -   configurer Eclipse :
        -   Eclipse doit s'éxécuter sur une JVM (Java Virtual Machine)
            \>= 9 (*Testé avec Java 12*), si nécessaire ajouter une
            ligne "-vm \<path_to_jvm_dir\>/bin/server/jvm.dll" au début
            du fichier eclipse.ini
        -   Vérifier qu'une jvm \>= 11 soit connue d'Eclipse
            1.  *Window -\> Préférences -\> Java -\> Installed JREs*
            2.  Si ce n'est pas le cas :
                1.  cliquer sur « Add… »
                2.  Sélectionner « Standard VM »
                3.  Renseigner le répertoire principale d'une JVM \>= 11
                4.  Cliquer « Finish »

### Recommandation

*Ces plugins Eclipse ne sont pas obligatoires mais sont fortement
recommandés pour l'utilisation du Plugin Qualinka. Pour les* *installer,
allez dans « Help » -\> « Eclipse Markeplace » puis faite une recherche
avec le nom du plugin et cliquer* *sur installer.*

![image](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/eclipse-plugin-json-logo.png) plugin JSON Editor Plugin

![image](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/eclipse-plugin-ansi-logo.png) plugin Ansi

### Installation du plugin

   
Une fois Eclipse lancé et configuré, allez dans "Help -\> Install New
Software…" :

1.  Cliquer sur « Add… » ;
2.  Cliquer sur « Local… » ;
3.  Sélectionner le répertoire «
    P:/Developpement/Eclipse/plugins/Qualinka » ;
4.  Donnez lui un nom, par exemple : « Qualinka » ;
5.  Cliquez sur « Add » ;
6.  Cochez « QualinkaPlugin », puis cliquez sur « Next \> » ;
7.  Suivez les instructions (cliquez sur « Next \> ») ;
8.  Cochez « I accept the terms of the license agreement », puis «
    Finish » ;
9.  Ignorez le warning « you are installing software that contains
    unsigned content… » en cliquant sur « Install anyway » ;
10. Cliquez « Restart Now ».

### Perspective « Qualinka »

Le plugin « Qualinka » fournit une perspective dédiée que vous pouvez
activer. Pour cela :

-   Cliquez sur l'icône représentant trois livres si celle-ci est
    visible (en haut à droite).

![Icône perspective QualinKa](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/plugin-icone-perspective.png)

-   Sinon cliquez sur l'icône « Open Perspective », Puis sélectionner
    « Qualinka ».

![Icône « Open Perspective »](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/plugin-icone-open-perspective.png)

*Note: si « Qualinka », n'apparaît pas dans la* *liste des perspectives,
cela signifie que le plugin n'est pas correctement installé* *ou que
Eclipse n'est pas correctement configuré pour le plugin (démarrage avec
une JVM \>= 9)*.

## Importation d'un projet

### Récupération initiale

1.  « File » -\> « Import… » ;
2.  « Git » -\> « Projects from Git » (ne pas sélectionner « Projects
    from Git (with smart import) ») ;
3.  « Clone URI » ;
4.  remplir le champs URI avec l'URL suivante
    <https://git.abes.fr/depots/sudoqualsudoc.git> (ou celle d'un autre
    projet QualinKa) ;
5.  Saisir votre identifiant/mot de passe Abes ;
6.  Cliquez « Next \> » ;
7.  Sélectionner seulement la branche "develop"
8.  Sélectionner « import existing eclipse projects »
9.  Cliquez « Next \> » puis « Finish ».

À ce stade vous devriez voir :

![Vue initiale](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/client-lourd-vue-initial.png)

### Mise à jour

Afin de récupérer la dernière version d'un projet SudoQual, il vous faut
dans un premier temps mettre de côté vos modifications locales (si vous
en avez). Pour cela, faites :

-   clique droit sur la racine du projet
-   choisir « Team \> »
-   « Stashes \> »
-   « Stash changes… »
-   Entrez un message afin d'identifier les changements sauvegardés.

![Stash changes](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/client-lourd-stash-changes.png)

Une fois cela fait, vous pouvez importer les mises à jour du projet.
Pour cela, faites :

-   clique droit sur la racine du projet
-   choisir « Team \> »
-   puis « Pull » (sans "…")
-   suivre les instructions

![Pull remote changes](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/client-lourd-pull.png)

Une fois les mise à jour du projet importer, vous pouvez éventuellement
réappliquer vos modifications locales. Pour cela faites :

-   clique droit sur la racine du projet
-   choisir « Team \> »
-   « Stashes \> »
-   sélectionner vos changements précédemment sauvegardés

![Select stashed changes](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/client-lourd-select-stashed-changes.png)

-   Cliquez sur l'icône « Apply Stashed Changes »

![Apply stashed changes](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/client-lourd-apply-stashed-changes.png)

Pour plus d'information sur la gestion des versions d'un projet,
veuillez vous référer à la documentation du plugin Eclipse « EGit » :
<https://www.eclipse.org/egit/documentation/>.

## Utilisation

### Vue projet « Qualinka »

Dans la vue « Project Explorer », vous devriez avoir votre projet
« Qualinka » semblable à l'image ci-dessous.

*Note: si la vue « Project Explorer » n'est pas ouverte, vous pouvez
l'ouvrir depuis « Window -\> Show view \> -\> Other… ».*

![Vue projet](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/plugin-project-view.png)

   
**Éléments de la vue projet :**

Raw  
Ce dossier est à destination des informaticiens ou utilisateurs avancés.
Il contient les dossiers et fichiers du projet tels qu'ils sont
naturellement sur le système de fichiers. Certains fichiers spécifiques
ne sont présent qu'à travers ce dossier, les fichiers couramment
utilisés sont accessibles à travers d'autres « dossiers ».

Benchs  
Ce dossier est destiné à contenir des fichiers JSON de benchmark pouvant
être éxécuter avec « Run Qualinka Eval ».

Criterions  
Ce dossier contient les fichiers de définition des critères.

Features  
Ce dossier contient les fichiers de définition des attributs pré-traités
et calculés. Les attributs bruts ne nécessite pas de fichier.

Filters  
Ce dossier contient les fichier de définition des filtres.

Heuristics  
Ce dossier est destiné à contenir les fichiers de définition des
heuristics spécifique, si besoin.

Resources  
Ce dossier est destiné à contenir les fichiers de resources pouvant être
utilisés par les critères, filtres, attributs ou heuristics. Par
exemples, des fichiers de « stop words », des dictionnaires de
fréquences d'apparition de mots ou encore des modèles de proximité entre
des mots.

Rules  
Ce dossier contient les fichiers de règles (fichier .dlp).

Scenarios  
Ce dossier contient les fichiers properties (.properties) permettant la
configuration d'un scénario.

Tests  
Ce dossier est destiné à contenir des fichiers d'entrées (.json) afin de
pouvoir tester le ou les scénarios du projet. Ces fichiers doivent
pouvoir s'éxécuter avec « Run Qualinka Link ».

Utils  
Ce dossier contient des fichiers Java contenant des classes
« utilitaires » pouvant être utilisée par les critères, filtres,
attributs et heuristics du projet.

### Exécution de « Qualinka »

L'exécution de « Qualinka » se fait depuis un fichier JSON (.json) :

-   soit par un clique droit sur un fichier JSON depuis la vue projet,
    puis « Run as \> » ;

![Exécution depuis la « vue
projet »](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/plugin-run-as-from-project-view.png)

-   soit par un clique droit sur le contenu d'un fichier JSON ouvert
    dans l'éditeur de texte, puis « Run as \> ».

![Exécution depuis l'éditeur de
texte](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/plugin-run-as-from-editor.png)

Le résultat de l'exécution s'affiche dans la vue « Console ».

![Résultat de l'exécution de
« Qualinka »](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/plugin-run-as-output.png)

Pour plus d'information sur les modes d'exécution se référer à la
section .

### Consultation/Édition d'un fichier de règles

Pour consulter ou éditer un fichier de règles, il vous suffit d'ouvrir
l'entrée « Rules » dans la vue projet et de double-cliquer sur le
fichier à éditer. Le fichier s'ouvre dans la partie droite de
l'application.

![Éditeur de fichier de règles](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/client-lourd-edition-rules.png)

   
**L'éditeur de règles proposent plusieurs fonctionnalités :**

1.  une analyse syntaxique, mise en évidence des erreurs de syntaxe

    ![](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/client-lourd-rules-syntaxe-error.png)    

2.  une analyse sémantique, mise en évidence des erreurs de référence
    (critère, filtre ou dimension inexistant ; seuil du critère
    incorrect…)

    ![](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/client-lourd-rules-sem-error.png)    

3.  l'affichage de la documentation au survol d'un critère ou filtre

    ![](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/client-lourd-rules-doc.png)    

4.  autocomplétion des noms de critères, filtres et dimensions
    (CTRL+espace)

    ![](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/client-lourd-rules-autocomplete.png)    
    Cette fonctionnalité est très importantes, car elle permet de
    connaître l'ensemble des prédicats disponibles. Pour cela, il suffit
    de commencer à écrire une règle (il faut avoir le curseur à un
    endroit où l'on attend un prédicat) puis faire "CTRL+espace".

    ![](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/client-lourd-rules-autocomplete-allpred.png)    

5.  raccourci d'accès au code Java d'un critère ou filtre (CTRL+clique
    gauche)

### Rafraîchissement du projet

Si les données du projet ne semble pas à jour, par exemple si une
tentative d'exécution d'un fichier JSON ne fournit aucune proposition
depuis le menu « Run As ». Vous pouvez essayer de rafraîchir le projet,
pour cela faites :

-   clique droit sur la racine du projet
-   refresh

## Utilisation avancée

### Exécution des attributs, filtres et critères

Afin de facilité la configuration d'un scénario et son débogage, le
plugin « Qualinka » propose une exécution indépendante de ces éléments
sur des données saisis à la volée.

1.  Attributs

    L'exécution d'un attribut se fait depuis un fichier d'attribut :

    -   soit par un clique droit sur un fichier JSON depuis la vue
        projet, puis « Run as \> » ;
    -   soit par un clique droit sur le contenu d'un fichier JSON ouvert
        dans l'éditeur de texte, puis « Run as \> ».

    ![Menu d'exécution d'un attribut](./fig/plugin-feature-exec.png)

    -   Sélectionner votre scénario et saisissez la valeur des données à
        tester. **Attention, les** **données saisie doivent présenter
        une structure valide par rapport aux attentes de l'attribut
        testé** **(chaîne de caractère, tableau, nombre entier, objet
        JSON)**.

    ![Fenêtre de saisie des
    données](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/plugin-feature-exec-data-input.png)

    *Note: la sélection du scénario n'a d'effet notable que si
    l'attribut testé* *présente une configuration différentes en
    fonction de celui-ci, par exemple* *l'utilisation d'un fichier de «
    mots rares » spécifique au scénario.*

2.  Attributs calculés

    Les données des attributs calculés peuvent être saisi de deux
    manières.

    1.  Les données sont saisis en tant que tableau d'objet JSON, chaque
        objet JSON représentant les données d'une référence liée par un
        `sameAs`. Par exemple :

    ``` json
    [
     {"publicationDate": 1928},
     {"publicationDate": 1903},
     {"publicationDate": 2001},
     {"publicationDate": 1933}
    ]
    ```

    ou encore si l'attribut calculés s'appuie sur plusieurs attributs :

    ``` json
    [
     {"domain": 621, "role": "author"},
     {"domain": 621, "role": "thesis_advisor"},
     {"domain": 620, "role": "author"},
    ]
    ```

    1.  Si (et seulement si), l'attribut calculés ne s'appuie que sur un
        seul attribut, alors les données d'entrées peuvent être saisies
        sous la forme d'un tableau de valeur (valeur de l'attribut
        d'appuie pour chacune des références liées).

    ``` json
    [ 1928, 1903, 2001, 1933 ]
    ```

3.  critères

    L'exécution d'un filtre ou critère se fait depuis un fichier de
    filtre ou de critère :

    -   soit par un clique droit sur un fichier JSON depuis la vue
        projet, puis « Run as \> » ;
    -   soit par un clique droit sur le contenu d'un fichier JSON ouvert
        dans l'éditeur de texte, puis « Run as \> ».

    Le champs de saisie de données est pré-rempli avec une structure
    JSON minimale présentant les différents attributs nécessaire pour la
    source et la cible. Il vous reste à remplacer les `?` par des
    données valides pour l'attribut correspondant.

    L'exemple ci-dessous montre cette structure pour le critère
    `personName` :

    ``` json
    {
        "source": {
            "personName": ?
        },
        "target": {
            "personName": ?
        }
    }
    ```

    Exemple de saisie valide :

    ``` json
    {
        "source": {
            "personName": ["Vernon, Sullivan"]
        },
        "target": {
            "personName": ["Vian, Boris", "Vernon, Sullivan"]
        }
    }
    ```

    L'exemple ci-dessous montre cette structure pour le critère
    `datePubLifeCA` :

    ``` json
    {
        "source": {
            "pubDate": ?
        },
        "target": {
            "birthUncertain": ?,
            "birth": ?,
            "death": ?
        }
    }
    ```

    Exemple de saisie valide :

    ``` json
    {
        "source": {
            "pubDate": "1986"
        },
        "target": {
            "birthUncertain": "1990-2000",
        }
    }
    ```

    Les attributs calculés doivent être saisis tel que décrit dans la
    section . L'exemple ci-dessous montre une entrée valide pour le
    critère `titleCA`.

    ``` json
    {
        "source": {
            "title": ["Foundation", "Fondation"]
        },
        "target": {
            "titleSA": [
              ["Prelude to Foundation", "Prélude à Fondation"], 
              ["Forward the Foundation", "L'Aube de Fondation"],
              ["Foundation and Empire", "Fondation et Empire"],
              ["Second Foundation", "Seconde Fondation"],
              ["Foundation's Edge", "Fondation foudroyée"],
              ["Foundation and Earth", "Terre et Fondation"]
            ]
        }
    }
    ```

4.  filtres

    Les filtres s'exécute de la même manière que les critères, se
    référer à la section précédente.

### Exécution en mode debug

Il est possible d'éxécuter « Qualinka » en mode « Debug » (mode pas à
pas), pour cela il suffit de sélectionner « Debug As » au lieu de « Run
As ». Pour plus d'information sur le mode « Debug » voir :

-   <https://www.vogella.com/tutorials/EclipseDebugging/article.html>

et/ou

-   <https://help.eclipse.org/2019-06/topic/org.eclipse.jdt.doc.user/concepts/cdebugger.htm>.

### Configuration du projet

Pour ouvrir la page de configuration, faites :

-   clique droit sur la racine du projet
-   « Properties »
-   Sélectionner « Qualinka »

![Entrée du menu permettant d'accèder à la page de
configuration](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/client-lourd-menu-properties.png)

Cette page vous permet de configurer la « classe Java » principal ainsi
que les différents dossiers accessibles à travers la vue projet. Enfin,
il permet de désactiver la vue projet « Qualinka » pour retrouver une
vue projet « Java » classique (plutôt pour les informaticiens).

![Page de configuration des propriétés du plugin pour le
projet](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/client-lourd-properties.png)

# Fonctionnement & Paramétrage

## Grandes fonctionnalités requises

Dans le but de répondre aux cas d'utilisation exprimés TODO 2 page 6, SudoQual
propose les grandes fonctionnalités suivantes :

### Alignement (one to one)

On cherche à « aligner » deux ensembles de données distincts
(bijection).

1.  entrée

    -   un ensemble de références sources,
    -   un ensemble de références cibles,
    -   un ensemble de références supports (optionnel),
    -   un ensemble de données pour chaque référence,
    -   un ensemble de liens sûrs entre les références sources ou cibles
        et les références supports.

2.  contraintes sur les données d'entrée

    -   sources, cibles et supports sont trois ensembles disjoints.

3.  sortie

    -   une liste de liens *sameAs*, *suggestedSameAs* ou *diffFrom*
        entre les références sources et les références cibles.

4.  contraintes sur les données de sortie

    -   une même référence source ou cible ne peut être présente que
        dans un seul lien *sameAs*.

### Affectation *ou liage* (many to one)

On cherche à « affecter » chaque élément de l'ensemble source à un
élément de l'ensemble cible (surjection).

1.  entrée

    -   un ensemble de références sources,
    -   un ensemble de références cibles,
    -   un ensemble de références supports (optionnel),
    -   un ensemble de données pour chaque référence,
    -   un ensemble de liens sûrs entre les références sources et cibles
        ou entre les références supports et cibles.

2.  contraintes sur les données d'entrée

    -   sources et cibles sont deux ensembles disjoints,

3.  sortie

    -   une liste de liens *sameAs*, *suggestedSameAs* ou *diffFrom*
        entre les références sources et les références cibles.

4.  contraintes sur les données de sortie

    -   une même référence source ne peut être présente que dans un seul
        lien *sameAs*.

### Regroupement (many to many)

On cherche à repérer des « groupes » dans un ensemble d'élements.

1.  entrée

    -   un ensemble de références « sources/cibles »,
    -   un ensemble de références supports (optionnel),
    -   un ensemble de données pour chaque référence,
    -   un ensemble de liens sûrs entre les références
        « sources/cibles » et supports.

2.  contraintes concernant les données d'entrée

    -   « sources/cibles » et supports sont deux ensembles disjoints,

3.  sortie

    -   une liste de liens *sameAs*, *suggestedSameAs* ou *diffFrom*
        entre les références « sources/cibles » fournies en entrée et un
        ensemble de références « virtuelles » représentant chacun un
        regroupement.

4.  contraintes sur les données de sortie

    -   une référence « sources/cibles » ne peut être présente que dans
        un seul lien *sameAs*.

## Module de liage

Le module de liage « linking module » est le cœur du framework SudoQual.
Il permet de proposer des liens de co-référence sûrs (*sameAs*), des
suggestions de liens de co-référence (*suggestedSameAs*) ou des liens de
non-co-référence (*diffFrom*). Pour créer ces liens, ce module s'appuie
sur un ensemble de **règles métiers** telles que :

-   « **SI** la **forme du nom** est proche **ET SI** un
    **co-contributeur** est homonyme **ET SI** un **titre** est assez
    proche, **ALORS** il est ***PROBABLE*** qu’il s’agisse de la même
    personne. »
-   « **SI** les **dates de publication** sont éloignées **ET SI** les
    **contenus** sont éloignées, **ALORS** il est ***TRÈS PROBABLE***
    qu'il ne s'agisse pas de la même personne. » .

Ces règles sont construites à partir d'un ensemble de **critères**
(*criterions*) et de **filtres** (*filters*) qui eux mêmes utilisent un
ensemble d'**feature** (*features*). La figure TODO 18 présente les différents
éléments du module de liage et leurs interactions.

![Composants de module de liage](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/image-043.png)

-   un **attribut** (*feature*) fournit une information concernant
    l'entité référencée. Par exemples : son nom, son prénom, sa date de
    naissance, ses co-contributeur ou encore une liste de mots-clés ;
-   un **critère** (*criterion*) permet de comparer des feature de deux
    référence. Par exemple, le critère *co-contributeur* permet
    d'évaluer si deux référence d'entités ont des co-contributeurs en
    commun.
-   un **filtre** (*filter*) permet de faire une évaluation booléenne
    (vrai ou faux) à partir d'un ensemble d'feature d'une référence
    unique. Par exemple, le filtre **authorOfThesisRA** permet de
    vérifier si l'entité référencée est l'auteur d'une thèse.

L'ensemble de règles est utilisé par SudoQual pour produire un ensemble
d'indices de co-référence (*sameAs*) et de non-co-référence (*diffFrom*)
pour chaque couple de référence d'entité fourni en entrée. Ces indices
sont ensuite utilisés à travers une **heuristique** pour asserter des
liens de co-référence ou de non-co-référence. L'heuristique peut
également *suggérer* des liens de co-référence.

Il est **important** de noter que pour l'**assertion de liens de
co-référence**, **on préférera toujours la précision sur le rappel**.
C'est à dire que l'on préférera ne pas proposer de lien plutôt que de
proposer un lien faux. Alors que pour la **suggestion de lien**, **on
préférera le rappel sur la précision**. C'est à dire que l'on préférera
proposer un lien faux plutôt que de ne pas proposer un lien correcte.

### Attributs

Chaque attribut utilisable par l'outil doit être explicitement déclaré
(sauf si l'option `undeclaredRawFeaturesEnabled` vaut `true`, dans ce
cas les attributs utilisés mais non déclarés seront considérés comme des
attributs bruts). La clé permettant d'y faire référence dans les données
d'entrées ou dans les filtres et critères doit être un identifiant
unique.

Les attributs ont pour but l'identification et le pré-traitement
(nettoyage/extraction) des données en vue de leur utilisation dans les
critères de comparaison.

1.  les différents types d'attributs

    attribut brut (raw feature) :  
    les données transmises aux filtres et critères sont strictement
    identiques aux données en entrée du module.

    attribut pré-traité (pre-processed feature) :  
    les données transmises à l'outil sont d'abord pré-traitées avant de
    pouvoir être transmises aux filtres et critères.

    attribut calculé (computed feature) :  
    les données de l'attribut sont calculées en cours de traitement à
    partir des liens de co-référence sûrs calculés par le système et
    d'une fonction d'aggregation des valeurs d'un ou de plusieurs
    feature de ses co-référents. **Contrairement aux autres types
    d'feature, la valeur d'un attribut calculé peut évoluer en cours de
    traitement**.

2.  Gestion des aggregation d'attributs

    Les attributs calculés peuvent utiliser des agrégations
    pré-implémentées en héritant des classes abstraites suivantes :

    **AJSONArrayUnionComputedFeature** :  
    Cette classe produit un attribut de type JSONArray représentant
    l'union de l'attribut spécifié (de type JSONArray) des références
    déclarées comme `SameAs`.

    **AObjectWeightedUnionComputedFeature** :  
    Cette classe produit un attribut de type JSONObject contenant
    l'union pondérée de l'attribut spécifié des références déclarées
    comme `SameAs`.

3.  Enrichissement

    L'utilisation d'attributs calculés permet « l'enrichissement » des
    attributs au cours de l'exécution de SudoQual. Chaque lien « X
    sameAs Y » produit par SudoQual sera utilisé pour produire les
    attributs calculés de X et de

    1.  

### Filtres et Critères

Un filtre ou un critère est une fonction définie sur un ensemble
d'feature, cet ensemble d'feature est transmis via une *référence* (la
*référence* sert de clé pour la récupération des feature associés).

Un filtre  
est une fonction prenant en entrée une valeur pour chaque attribut d'un
ensemble d'feature et qui retourne soit VRAI, soit FAUX. La valeur des
feature lui est transmise à travers une *référence*.

Un critère  
est une fonction prenant en entrée une valeur pour chaque attribut de
deux ensembles d'feature. Le co-domaine d'un critère est un ensemble
d'entiers relatifs fini qui est défini pour chaque critère.

1.  Gestion des critères

    Les critères compare deux références X et Y sur un « axe » donné,
    par exemple : le titre, la date de publication, le nom de l'auteur…

    Actuellement, il existe un seul critère abstrait :

    **ABestArrayElementComparisonCriterion** :  
    Ce critère abstrait permet de comparer deux à deux les éléments de
    deux tableaux (à travers la méthode abstraite `compareElement`) et
    de retourner une valeur de critère associée au résultat de la
    meilleure valeur de comparaison trouvée (à travers
    `mapComparisonValueToCriterionValue`).

### Règles

Le jeu de règles permet de combiner les critères pour définir les
conditions suffisantes pour la déduction d'un lien *sameAs*,
*suggestedSameAs* ou *diffFrom*.

Pour plus d'information, consultez la section TODO 7.3 page suivante.

### Scénarios

La notion de scenario correspond à un paramétrage du module de liage. Un
scenario est principalement défini par son *jeu de règles*, en effet
celui-ci défini indirectement les *filtres* et *critères* utilisés (par
l'intermédiaire des *prédicats calculés*) qui eux-mêmes définissent
indirectement les *feature* utilisés. Il reste donc à définir le *mode
de liage* et la valeur par défaut des options pour définir un scenario
complet.

Plusieurs scénarios peuvent éventuellement être mis en œuvres pour la
réalisation d'un cas d'usage.

*Note: La configuration des scénarios est effectuée via des fichiers
"properties".*

1.  Configuration

    *\[OPT\] signifie que le champ est optionnel.*

    La configuration d'un scénario se fait via un fichier ".properties"
    contenant les entrées ci-dessous. Le nom du fichier définit le nom
    du scénario.

    -   ruleset: String - *nom du fichier de règles utilisé*
    -   heuristic: String - *\[OPT\] nom de la classe d'heuristic à
        utiliser. La valeur par défaut est "default"*
    -   heuristicMode: String - *mode d'heursitic à utiliser: oneToOne,
        oneToMany, manyToOne.*
    -   businessClassPackage: String - *\[OPT\] le package Java dans
        lequel le module cherchera les classes métiers (Features,
        Filters, Criterions, LinkHeuristics). La valeur par défaut est
        "fr.abes.sudoqual.linking_module"*
    -   validatedSameAsThreshold : int - *seuil pour qu'un lien "sameAs"
        puisse être validé.*
    -   suggestedSameAsThreshold : int - *seuil pour qu'un lien "sameAs"
        puisse être suggéré.*
    -   validatedDiffFromThreshold : int - *seuil pour qu'un lien
        "diffFrom" puisse être validé.*
    -   suggestedEnabled : boolean - *activation des liens suggérés.*
    -   keepOnlyBestSuggestions : boolean - *si activé, conserve
        seulement le résultat de confidence la plus élevée, sinon la
        totalité des suggestions trouvées seront retournées.*
    -   businessClassPackage : String - *\[OPT\] nom du package Java où
        chercher les classes d'attributs, filtres, critères et
        heuristics ex: "fr.abes.sudoqual".*
    -   exportCriterionValues : boolean - *activation de l'export de la
        valeur des critères calculés lors de l'exécution du module.*

    *Note: différents scénarios peuvent utiliser un même fichier de
    règles.*

       
    Exemple de fichier de configuration :

    ``` shell
    # rule set filename
    ruleset=sudoqual1-rc-ra.dlp

    # [optional] specific heuristic name
    heuristic=default

    # [optional] Java package name where features, filters, criterions and heuristics will be searched
    businessClassPackage=fr.abes.sudoqual

    # heuristic mode
    heuristicMode=MANY_TO_ONE

    # threshold to validate a same-as link
    validatedSameAsThreshold=5

    # threshold to validate a suggested same-as link
    suggestedSameAsThreshold=1

    # threshold to validate a different-from link
    validatedDiffFromThreshold=6

    # enable suggested same-as
    suggestedEnabled=true

    # if enable, keep only suggestedSameAs or sameAs with the best confidence
    keepOnlyBestSuggestions=true

    # export computed criterion values to cache purpose
    exportCriterionValues=false
    ```

## Moteur de règles

### Introduction

1.  Qu'est-ce qu'un moteur de règles ?

    Lorsque l'on parle de **règles**, on fait référence à une
    implication en [logique
    mathématique](https://fr.wikipedia.org/wiki/Implication_(logique)).
    Une règle est de la forme `P -> Q`, **si** `P` est `VRAI` **alors**
    `Q` doit aussi être `VRAI`. La partie de gauche est appelée *corps*
    ou *hypothèse*, la partie de droite *tête* ou *conclusion*. `P` et
    `Q` sont appelés *atomes*.

    La règle logique `P -> Q` peut-être interprétée dans un système
    informatique comme :

    -   une *contrainte*, si `P` est `VRAI` et `Q` ne l'est pas alors
        l'ensemble de données est incohérent ;
    -   comme une règle *génératrice*, si `P` est `VRAI` et `Q` ne l'est
        pas alors on ajoute `Q` à l'ensemble des données.

    Dans un **moteur de règles**, les règles sont utilisées comme des
    *génératrices de données*.

    ``` text
    P -> Q
    Q -> R
    S -> T
    U -> V
    ```

    Ainsi, à partir de l'ensemble de règles ci-dessus et l'ensemble de
    données de départ "`{ P, S }`", un *moteur de règles* produira
    l'ensemble de données "`{ P, Q, R, S, T }`". L'ensemble de données
    obtenues ne contient pas `V` car la règle `U -> V` ne rencontre pas
    les conditions nécessaires à son *déclenchement*, à savoir la
    présence de `U` dans l'ensemble de données. Par contre celui-ci
    contient `R` car la règle `Q -> R` se *déclenche* malgré l'absence
    de `Q` dans l'ensemble de données initial, celui-ci est produit par
    `P -> Q`.

2.  Complexifions un peu les règles

    Les règles présentées dans la partie précédente sont volontairement
    simplifiées. Les règles utilisées par le moteur de règles en
    diffèrent de deux manières :

    1.  Elles acceptent une *conjonction d'atomes* en *hypothèse*, c'est
        à dire un ensemble d'atomes qui doivent tous être `VRAI` pour
        que l'hypothèse soit considérée `VRAI` et la règle déclenchée.
        On écrira les atomes de l'hypothèse en les séparant par des
        virgules représentant un `ET` logique. Une règle peut donc être
        de la forme "`P, Q, R -> S`", cette règle produira `S` si `P`,
        `Q` et `R` sont tous les trois `VRAI`.

    2.  Jusqu'ici nos règles sont exprimées en [logique des
        propositions](https://fr.wikipedia.org/wiki/Calcul_des_propositions).
        Les règles que nous considérerons sont un fragment de la
        [logique du première
        ordre](https://fr.wikipedia.org/wiki/Calcul_des_pr%C3%A9dicats),
        cette logique accepte l'utilisation de *variables* et de
        *constantes*. Voici une règle exprimée dans ce fragment :
        "`P(X), Q(X,Y) -> R(Y)`", ici `X` et `Y` sont des *variables*.
        Un ensemble de données devient donc de la forme
        "`{ P(a), P(c), Q(a,b), Q(d,e) }`", ici `a`, `b`, `c`, `d` et
        `e` sont des *constantes*.

    **`Q(X,Y)` est donc un *atome* de *prédicat* `Q` prenant** **deux
    *variables* comme paramètres.**

3.  Un exemple concret

    ``` text
    book(X), writerOf(X, Y) -> writer(Y).
    ```

    Soit la règle ci-dessus (*Si un livre X a été écrit par Y, alors Y
    est un écrivain*), et l'ensemble de données
    `{book(the_hitchhiker_s_guide_to_the_galaxy),`
    `writerOf(the_hitchhiker_s_guide_to_the_galaxy, douglas_Adams)}`,
    alors le *moteur de règles* en déduira `{ writer(douglas_Adams) }`.

4.  Expression du besoin

    L'objectif de l'utilisation d'un moteur de règles dans le programme
    SudoQual est de produire, à partir d'un ensemble de métiers, des
    **indices** de ou de pour des couples de d'autorités. Ces *indices*
    seront ensuite utilisés par l'*heuristic* du module de liage pour
    établir des liens sûrs ou suggérés.

    Le moteur de doit pouvoir évaluer un ensemble de règles *Datalog*
    non *récursif* et être capable d'évaluer des ** dont la fonction
    d'évaluation est implémentée à travers une classe Java spécifique.
    Dans le projet, les utilisés en ** de règles qui ne sont pas des **
    sont appelés des **. Les ** présents seulement en ** sont les ** qui
    ont vocation à être interrogé par SudoQual.

    Le moteur de règles doit pouvoir répondre à des requête de type :
    *quel est l'indice maximal pouvant être déduit pour le prédicat `P`
    et les références `ref1` et `ref2`*. Pour cela, le moteur de règles
    doit avoir connaissance de l'ensemble des données rattachées à
    chaque , elles lui sont passées en même temps que la requête à
    évaluer à travers une implémentation de l'interface Java
    `FeatureManager`.

    Ci-dessous, un extrait d'un jeu de règles écrit pour SudoQual qui
    doit pouvoir être évalué par le moteur. Les types de requêtes
    pouvant être posées sur ce jeu de règles sont :

    -   Quelle est la valeur maximal de `X` tel que
        "`diffFrom(ref1, ref2, X)`" soit `VRAI` ?
    -   Quelle est la valeur maximal de `X` tel que
        "`sameAs(ref1, ref2, X)`" soit `VRAI` ?

    ``` text
    % diffFrom
    [DIn1] diffFrom(S,T,never) :- dim_dates(S,T,never).
    [DI51] diffFrom(S,T,5) :- personNameCA(S,T,-1).
    [DI52] diffFrom(S,T,5) :- dim_contenu(S,T,-1), dim_dates(S,T,-1).

    % sameAs
    [IDa1] sameAs(S,T,always) :- sourceCA(S,T,always).
    [IDa2] sameAs(S,T,always) :- dateCreationNoticeCA(S,T,always)
    [ID63] sameAs(S,T,6) :- personNameCA(S,T,2), cocontribNameCA(S,T,2).
    [ID51] sameAs(S,T,5):- personNameCA(S,T,2), dim_contenu(S,T,3).

    % dimensions
    [DateDim1] dim_datesDimension(S,T,-1) :- datePubCA(S,T,-1).
    [DateDim5] dim_datesDimension(S,T,never) :- datePubLifeCA(S,T,never).
    [ContDim1] dim_contenuDimension(S,T,-1) :- deweyCA(S,T,-1).
    [ContDim22] dim_contenuDimension(S,T,3) :- titleCA(S,T,1), 
            rameauKeywordCA(S,T,1),  domainCA(S,T,1).
    ```

    Dans cet extrait :

    -   `diffFrom` et `sameAs` représentent respectivement une
        indication de *distance* et de *similarité* que l'on veut
        obtenir à partir du jeu de règles ;
    -   `S` et `T` sont des ** qui seront substituées par une ** lors de
        l'évaluation, `S` sera substituée par une ** « source » et `T`
        sera substituée par une ** « cible » (*Target*) ;
    -   `personNameCA`, `sourceCA`, `dateCreationNoticeCA`, `datePubCA`,
        `datePubLifeCA`, `deweyCA`, `titleCA`, `domainCA` sont tous des
        ** qui évaluent la proximité/distance de deux sur un axe donné
        (noms, dates, titres…) ;
    -   `dim_dates` et `dim_contenu` sont des *dimensions* permettant
        d'abstraire plusieurs critères traitant d'une même thématique ;

    \+ les lignes commençant par `%` sont des commentaires.
    1.  Requête atomique

        Notre moteur de règles doit pouvoir répondre à des requêtes
        atomiques (comprenant un seul atome) du type : *Quel est la
        valeur maximal de `X` tel que "`p(ref1, ref2, X)`" soit `VRAI`
        ?* Ici, `p` est le nom d'un prédicat standard (non calculé),
        `ref1` et `ref2` des références, `X` une variable dont on
        cherche la substitution en valeur entière la plus grande
        possible tel que `p(ref1, ref2, X)` soit `VRAI`.

### Syntaxe des règles

La syntaxe des règles acceptées correspond à une restriction de la
syntaxe `Dlgp v2` (<http://graphik-team.github.io/graal/doc/dlgp>). Nous
présentons ici la syntaxe de manière informelle, pour une définition
formelle se référer à la section TODO 8.1.3 page 68.

La syntaxe utilisée hérite de la syntaxe
[datalog](https://en.wikipedia.org/wiki/Datalog). Les règles sont de la
forme "`conclusion(X,Y) :- hypothesis(X,Y).`". À noter :

-   l'hypothèse et la conclusion sont inversées par rapport à la syntaxe
    intuitive présentée en introduction
    ("`hypothesis(X,Y) -> conclusion(X,Y)`") ;
-   le symbole d'implication '`->`' devient '`:-`' ;
-   une règle se termine par un '`.`' ;
-   le caractère '`%`' introduit un commentaire.
-   il est possible de faire précéder une règle d'un label entre
    crochets : '`[label]`'

1.  Notre exemple « concret » précédent devient donc :

    ``` text
    % Si un livre X a été écrit par Y, alors Y est un écrivain
    writer(Y) :- book(X), writerOf(X, Y).
    ```

2.  Les règles suivantes sont **interdites** :

    ``` text
    % Deux atomes en conclusion d'une règle (peut-être remplacée par deux règles)
    book(X), human(Y) :- writerOf(X, Y).

    % Une variable n'apparaissant que en conclusion (règle existentielle)
    writerOf(X, Y) :- book(X).

    % Une variable n'apparaissant que en hypothèse
    human(X) :- writerOf(Y, X).
    ```

### Utilisation par le module de liage

Pour rappel, une règle est composée d'une hypothèse (ou corps) et d'une
conclusion (ou tête). L'hypothèse est composée d'un ensemble d'*atomes*,
la conclusion est composée d'un seul atome. Un atome est composé d'un
*prédicat* et d'une liste de paramètres (non vide), un atome est soit
*vrai*, soit *faux*. **Si** l'ensemble des atomes du corps de la règle
sont évalués *vrai* **alors** l'atome de la conclusion sera également
évalué *vrai*.

Dans notre utilisation, il existe deux types de prédicats :

-   les **prédicats calculés** : leur nom fait référence à un filtre ou
    un critère. Ils peuvent être préfixé par "not\_" pour inverser leur
    valeur. Exemple : `sourceCriterion`
-   les **prédicats standards** ou **dimensions** : leurs noms sont
    préfixés par "dim\_". Ils sont utilisés dans SudoQual pour abstraire
    plusieurs **prédicats calculés** concernant une même thématique. Par
    exemple, la dimension `publicationEnvironment` pourrait être
    utilisée pour abstraire un critère vérifiant si l'éditeur est
    identique et un autre critère vérifiant si il existe des
    collectivités identiques liées aux autorités comparées. Il sera
    alors possible d'utiliser ce nouveau prédicat pour vérifier si au
    moins un des critères est vrai, voir assigner à ce nouveau prédicat
    un indice plus fort lorsque les deux sont vrais.

Par extension, il existe deux types d'atomes :

-   les **atomes calculés** : ils permettent d'appliquer le filtre ou le
    critère référencé par un **prédicat calculé** à une ou deux
    référence et vérifier si le résultat obtenu est supérieur ou égal à
    un seuil. Exemple : `sourceCriterion(X,Y,1)`.
-   les **atomes standards** ou **atomes de dimension** : de la même
    façon ils permettent de vérifier si il existe une combinaison
    d'**atomes calculés** permettant de générer l'atomes standard
    attendu avec un indice au moins égal au seuil demandé. Exemple :
    `publicationEnvironment(X,Y,2)`.

Contraintes sur le jeux de règles :

-   les *atomes calculés* ne peuvent être utilisés que en corps de
    règle.
-   les *atomes de dimension* apparaissant en corps de règle doivent
    également apparaître dans au moins une tête d'une autre règle.

1.  Filtres, critères et dimensions

    Les filtres, critères et dimensions sont référencés dans le jeu de
    règles à travers le nom des prédicats.

2.  Filtres

    Un *filtre* sert à vérifier une caractéristique dans les données
    rattachée à une référence. Un filtre prend en paramètre une
    référence et retourne une valeur booléenne (`VRAI` ou `FAUX`).

    Un atome utilisant un prédicat calculé de type *filtre* est évalué
    `VRAI` si et seulement si l'évaluation de la fonction Java pour la
    référence passée en paramètre renvoie `VRAI`.

3.  Critères

    Un *critère* sert à évaluer la proximité ou la distance de deux
    *références* sur un axe particulier, par exemple le nom, la date de
    naissance, la date de publication, les mots clés… L'exécution d'un
    critère pour deux *références* données produira une valeur entière
    appartenant à l'ensemble des valeurs de retour possibles défini pour
    ce critère.

    Un atome utilisant un prédicat calculé de type *critère* est évalué
    `VRAI` si et seulement si le seuil attendu (3ème paramètre) est
    strictement positif et l'évaluation de la fonction Java renvoie une
    valeur supérieur ou égale, le seuil attendu est strictement négatif
    et l'évaluation de la fonction Java renvoie une valeur inférieur ou
    égale, le seuil attendu est 0 et l'évaluation de la fonction Java
    renvoie 0.

4.  Dimensions

    Une *dimension* sert à abstraire plusieurs critères traitant d'une
    même thématique. Elle permet par exemple :

    -   d'avoir un atome évalué à vrai si au moins un *critère* est vrai
        parmi un ensemble de *critères*. Par exemple :
        `contenuDimension(X,Y,1)` est vrai si `titleCriterionCA(X,Y,1)`
        ou `rameauKeywordCriterionCA(X,Y,1)` ou
        `domainCriterionCA(X,Y,1)` est vrai.
    -   d'obtenir un indice plus fort sur une thématique en combinant
        plusieurs indices fournis par des *critères* différents traitant
        de cette thématique. Par exemple : `contenuDimension(X,Y,3)` est
        vrai si `titleCriterionCA(X,Y,1)` et
        `rameauKeywordCriterionCA(X,Y,1)` et `domainCriterionCA(X,Y,1)`
        sont vrai.

    Un atome utilisant un prédicat de type *dimension* "`dim(X,Y,I)`"
    (avec I un indice entier) est évalué `VRAI` si et seulement si un
    atome de même dimension "`dim(X,Y,J)`" (avec J \>= I si I \> 0 ; J =
    0 si I = 0 ; ou J \<= I si I \< 0 ) peut-être déduit par les
    mécanismes classiques d'un moteur de règles, à savoir si il existe
    une règle dont cet atome est la conclusion et que l'ensemble des
    atomes de son hypothèse sont évalué à `VRAI`.

    À noter qu'une *dimension* peut éventuellement s'appuyer sur d'autre
    *dimensions*, l'évaluation d'une telle règle nécessitera donc
    d'évaluer ces autres *dimensions* en cascade. Cependant, les cycles
    sont interdits, une règle concluant à un atome de type *dimension*
    ne peut pas utiliser cette même dimension dans son hypothèse, ou
    indirectement nécessité l'évaluation d'un atome basé sur cette même
    dimension.

5.  NOT

    Un atome représentant un critère peut être préfixé par "`not_`", ça
    valeur logique est alors inversée. Par exemple, si
    "`personNameCriterionCA(X, Y, 100)`" est `vrai` alors
    "`not_personNameCriterionCA(X, Y, 100)`" est `faux` et inversement.

    Un atome représentant un filtre peut également être préfixé par
    "`not_`", ce qui produira le même comportement que pour un critère.

    Les atomes représentant une dimension ne peuvent pas être préfixé
    par "`not_`".

6.  Example réel (Extrait)

    ``` text
    % Il est très peu probable que 2 références n'ayant que des appellations
    % respectives non-homonymes représentent la même personne.
    [DI51] diffFrom(X,Y,5) :- personName(X,Y,-1).

    % Il est très peu probable que 2 références ayant publié dans des domaines
    % éloignés et à des périodes éloignées représentent la même personne.
    [DI52] diffFrom(X,Y,5) :- dim_contenu(X,Y,-1), dim_dates(X,Y,-1).

    % Il est certain que 2 références dont les notices ont été créées le même jour et
    % qui sont liées par le catalogueur représentent la même personne.
    [IDa2] sameAs(X,Y,7):- dateCreationNotice(X,Y, always).

    % Il est quasiment certain que 2 références dont les titres sont identiques
    % représentent la même personne.
    [ID61] sameAs(X,Y,6):- personNameCriterion(X,Y,2), dim_contenu(X,Y,4).

    %------------
    % DIMENSIONS
    %------------

    [ContDim1] dim_contenu(X,Y,-1) :- deweyCA(X,Y,-1).
    [ContDim4] dim_contenu(X,Y,-1) :- domainCA(X,Y,-1).
    [ContDim2] dim_contenu(X,Y,1) :- deweyCA(X,Y,1).
    [ContDim5] dim_contenu(X,Y,1) :- domainCA(X,Y,1).
    [ContDim3] dim_contenu(X,Y,2) :- deweyCA(X,Y,2).
    % [...]
    [ContDim22] dim_contenu(X,Y,4) :- titleCA(X,Y,2),rameauKeywordCA(X,Y,1),domainCA(X,Y,1).
    [ContDim23] dim_contenu(X,Y,4) :- titleCA(X,Y,1),rameauKeywordCA(X,Y,2),deweyCA(X,Y,1).
    [ContDim24] dim_contenu(X,Y,4) :- titleCA(X,Y,1),rameauKeywordCA(X,Y,2),domainCA(X,Y,1).

    [DateDim5] dim_dates(X,Y,never):- datePubLifeCA(X,Y,never).
    [DateDim7] dim_dates(X,Y,-2):- datePubLifeCA(X,Y,-1),datePubCA(X,Y,-1).
    [DateDim6] dim_dates(X,Y,-1):- datePubLifeCA(X,Y,-1).
    [DateDim1] dim_dates(X,Y,-1):- datePubCA(X,Y,-1).
    [DateDim2] dim_dates(X,Y,1):- datePubCA(X,Y,1),datePubLifeCA(X,Y,1).
    [DateDim3] dim_dates(X,Y,1):- datePubLifeCA(X,Y,1).
    [DateDim4] dim_dates(X,Y,2):- datePubCA(X,Y,2).

    ```

## Paramétrage de SudoQual

SudoQual est un **framework d'applications** et non une application en
soit. Il est donc nécessaire de « paramétrer » SudoQual pour un cas
d'usage avant de pouvoir l'utiliser. Pour cela, nous proposons les
étapes suivantes :

1.  spécification du cas d'usage ;
2.  choix d'un mode de liage : "one to one", "one to many", "many to
    one", "many to many" ;
3.  analyse des feature disponibles ;
4.  définition des critères réalisables à partir cet ensemble d'feature
    ;
5.  définition et rédaction d'un jeu de règles ;
6.  implémentation des critères et feature nécessaires ;
7.  chargement de ce paramétrage dans l'outil ;

## Module de diagnostic (many-to-one)

Ce module attribue un diagnostic à chaque référence *source* considérée.
Ce diagnostic prend la forme d'un **statut** et d'un identifiant de
**cas**, parfois accompagnées d'informations complémentaires :

**initialLink**:  
indique la référence *cible* à laquelle est liée la référence *source*
dans le jeu de données initial ;

**computedLink**:  
indique la référence *cible* identifiée par SudoQual ;

**suggestedLinks**:  
indique un ensemble de référence *cibles* pour lequel SudoQual a trouvé
des indices de co-référence mais pas assez fort pour en faire un lien
sûr ;

**impossibleLinks**:  
indique un ensemble de référence *cibles* identifiées comme
**impossibles** par SudoQual.

5 statuts peuvent être attribués à une référence source :

**validatedLink**:  
le lien initiale est validé par SudoQual (cf. cas 1)

**almostValidatedLink**:  
le lien initiale est jugé fort probable par SudoQual (cf. cas 6)

**doubtfulLink**:  
le lien initiale est jugé douteux par SudoQual (cf. cas 7 et 8)

**erroneousLink**:  
le lien initiale est jugé impossible par SudoQual (cf. cas 2, 3, 4 et 5)

**missingLink**:  
aucun lien n'était présent dans la base, SudoQual peut faire des
propositions de correction (cf. cas 9, 10, 11 et 12)

   
\*Détails des 12 cas\* : TODO pas clair

Afin de définir ces 12 cas, nous nous appuyons sur les informations
suivantes calculées par SudoQual pour une référence *source* donnée :

**initial link (AL) :**  
la référence *cible* liée à la *source* dans la base ;

**computed link (CL) :**  
la référence *cible* liée de façon sûre à la *source* par SudoQual ;

**suggested links (SL) :**  
l'ensemble de référence *cibles* suggérées comme liens possibles pour la
*source* (cf. **suggestedLink**) ;

**impossible links (IL) :**  
l'ensemble de référence *cibles* jugées de façon sûre par SudoQual comme
impossibles pour la *source* (cf. **impossibleLink**) ;

**candidate authorities (CA) :**  
l'ensemble de toutes les référence *cibles*.

Par ailleurs, nous faisons l'hypothèse que SudoQual nous assure les
propriétés suivantes :

-   **CL** ∉ **IL** : un lien calculé ne peut pas appartenir à
    l’ensemble des liens impossibles.
-   **CL** ∉ **SL** : un lien calculé n’appartient pas à l’ensemble des
    liens proposés, la relation de lien calculé est plus forte que celle
    de lien suggéré.
-   **SL** ∩ **IL** = Ø : un lien suggéré ne peut pas être interdit.
-   **SL** ∪ {**CL**} ∪ **IL** ⊆ **CA** : on ne retourne comme *cible*
    calculée, suggérée ou interdite que des *cibles* appartenant à
    l'ensemble des *cibles* candidates.

Nous énumérons les 12 cas différents identifiés et spécifions pour
chacun d'entre eux le diagnostic renvoyé. ∃ **AL** signifie qu'un lien
initial existe (i.e. la référence contextuelle possède un lien dans la
base). ∃ **CL** signifie qu'un lien calculé a été établi.

1.  ∃ **AL** ∧ ∃ **CL** ∧ **CL** = **AL** –\> Lien validé  
    la *source* a un lien initial et un lien calculé, et ces liens
    coïncident : le lien est validé.  
    status : **validatedLink**  
    case : **1**  
    (mandatory) initialLink : **AL**  
    (mandatory) computedLink : **CL**  
    ~~(off) suggestedLinks : **SL**~~  
    *(optional) impossibleLinks : **IL***  

2.  ∃ **AL** ∧ ∃ **CL** ∧ **CL** ≠ **AL** –\> Lien erroné à corriger en
    **CL**  
    la *source* a un lien initial et un lien calculé, et ces liens ne
    coïncident pas : le lien est considéré comme erroné.  
    status : **erroneousLink**  
    case : **2**  
    (mandatory) initialLink : **AL**  
    (mandatory) computedLink : **CL**  
    ~~(off) suggestedLinks : **SL**~~  
    *(optional) impossibleLinks : **IL***

3.  ∃ **AL** ∧ ¬∃ **CL** ∧ **AL** ∈ **IL** ∧ **SL** ≠ {} (∧ **CA** ≠
    **IL**) –\> Lien erroné à corriger en **SL**  
    la *source* a un lien initial, n'a pas de lien calculé, mais le lien
    initial est jugé comme impossible par SudoQual : on signale que le
    lien est erroné et on indique les corrections suggérées.  
    status : **erroneousLink**  
    case : **3**  
    (mandatory) initialLink : **AL**  
    ~~(off) computedLink : **CL**~~  
    (mandatory) suggestedLinks : **SL**  
    (mandatory) impossibleLinks : **IL**

4.  ∃ **AL** ∧ ¬∃ **CL** ∧ **AL** ∈ **IL** ∧ **SL** = {} ∧ **CA** =
    **IL** –\> Lien erroné à corriger en **Nouvelle référence cible**  
    la *source* a un lien initial, n'a pas de lien calculé, mais le lien
    initial et toutes les autres cibles candidates sont jugées comme
    impossibles par SudoQual : on signale que le lien est erroné, il
    faudrait créer une nouvelle référence cible.  
    status : **erroneousLink**  
    case: **4**  
    initialLink : **AL**  
    ~~(off) computedLink : **CL**~~  
    ~~(off) suggestedLinks : **SL**~~  
    (mandatory) impossibleLinks : **IL**

5.  ∃ **AL** ∧ ¬∃ **CL** ∧ **AL** ∈ **IL** ∧ **SL** = {} ∧ **CA** ≠
    **LI** –\> Lien erroné à corriger  
    la *source* a un lien initial, n'a pas de lien calculé, mais le lien
    initial est jugé comme impossible par SudoQual et il n'y a pas de
    référence cible suggérée : on signale que le lien est erroné.  
    status : **erroneousLink**  
    case : **5**  
    initialLink : **AL**  
    ~~(off) computedLink : **CL**~~  
    ~~(off) suggestedLinks : **SL**~~  
    (mandatory) impossibleLinks : **IL**

6.  ∃ **AL** ∧ ¬∃ **CL** ∧ **AL** ∉ **IL** ∧ **SL** ≠ {} ∧ **AL** ∈
    **SL** –\> Lien quasi validé  
    la *source* a un lien initial, pas de lien calculé, mais le lien
    initial appartient aux liens suggérés : on indique que le lien est
    plutôt de bonne qualité.  
    status : **almostValidatedLink**  
    case : **6** initialLink : **AL**  
    ~~(off) computedLink : **CL**~~  
    (mandatory) suggestedLinks : **SL**  
    *(optional) impossibleLinks : **IL***

7.  ∃ **AL** ∧ ¬∃ **CL** ∧ **AL** ∉ **IL** ∧ **SL** = {} –\> Lien
    douteux  
    la *source* a un lien initial, mais n'a ni lien calculé, ni lien
    suggéré, et le lien initial n'est pas jugé comme impossible : on
    indique que le lien est douteux mais reste possible.  
    status : **doutbfulLink**  
    case : **7**  
    initialLink : **AL**  
    ~~(off)computedLink : **CL**~~  
    ~~(off) suggestedLinks : **SL**~~  
    *(optional) impossibleLinks : **IL***

8.  ∃ **AL** ∧ ¬∃ **CL** ∧ **AL** ∉ **IL** ∧ **SL** ≠ {} ∧ **AL** ∉
    **SL** –\> Lien douteux avec suggestion de correction en **SL**  
    la *source* a un lien initial, n'a pas de lien calculé, mais a des
    liens suggérés, et le lien initial bien que n'étant pas jugé comme
    impossible n'appartient pas aux liens suggérés : on indique que le
    lien est (très) douteux et on suggère de corriger avec les liens
    suggérés.  
    status : **doutbfulLink**  
    case : **8**  
    initialLink : **AL**  
    ~~(off) computedLink : **CL**~~  
    (mandatory) suggestedLinks : **SL**  
    *(optional) impossibleLinks : **IL***

9.  ¬∃ **AL** ∧ ∃ **CL** –\> Lien absent à compléter avec **CL**  
    la *source* n'a pas de lien initial, mais a un lien calculé : le
    lien est absent et on indique la correction.  
    status : **missingLink**  
    case : **9**  
    ~~(off) initialLink : **AL**~~  
    (mandatory) computedLink : **CL**  
    *(optional) suggestedLinks : **SL***  
    *(optional) impossibleLinks : **IL***

10. ¬∃ **AL** ∧ ¬∃ **CL** ∧ **CA** = **IL** –\> Lien absent à compléter
    avec **Nouvelle référence cible**  
    la *source* n'a ni lien initial, ni lien calculé et toutes les
    cibles candidates sont jugées comme impossible : le lien est absent
    et il faudrait créer une nouvelle référence cible  
    status : **missingLink**  
    case : **10**  
    ~~(off) initialLink : **AL**~~  
    ~~(off) computedLink : **CL**~~  
    ~~(off) suggestedLinks : **SL**~~  
    *(optional) impossibleLinks : **IL***

11. ¬∃ **AL** ∧ ¬∃ **CL** ∧ **CA** ≠ **IL** ∧ **SL** = {} –\> Lien
    absent difficile à compléter  
    la *source* n'a ni lien initial, ni lien calculé, aucune référence
    n'est suggéré mais quelques cibles candidates sont possibles : le
    lien est absent et aucune suggestion de complétion n'est donnée  
    status : **missingLink**  
    case : **11**  
    ~~(off) initialLink : **AL**~~  
    ~~(off) computedLink : **CL**~~  
    ~~(off) suggestedLinks : **SL**~~  
    *(optional) impossibleLinks : **IL***

12. ¬∃ **AL** ∧ ¬∃ **CL** ∧ **CA** ≠ **IL** ∧ **SL** ≠ {} –\> Lien
    absent à compléter en **SL**  
    la *source* n'a ni lien initial, ni lien calculé, mais quelques
    liens sont suggérés : le lien est absent et il est proposé de le
    compléter avec l'un des liens suggérés  
    status : **missingLink**  
    case : **12**  
    ~~(off) initialLink : **AL**~~  
    ~~(off) computedLink : **CL**~~  
    (mandatory) suggestedLinks : **SL**  
    *(optional) impossibleLinks : **IL***

## Évaluation (benchmark)

### Hypothèses prises

-   Il n'y a pas de doublon de notices d'autorité (NA),
-   Le diagnostic cible (diagnostic attendu) est le meilleur possible
    c'est à dire que si SudoQual valide des liens supplémentaires c'est
    qu'il prend trop de risque (ou que notre diagnostic attendu est à
    vérifier).

### Spécifications d'un fichier de Benchmark

Un fichier de benchmark est un fichier JSON constitué de deux entrées de
premier niveau :

input  
contient l'objet JSON correspondant à l'entrée du module de liage (cf.
).

expectedLinks  
contient un tableau JSON dont les spécifications sont les mêmes que
celles de la partie *computedLinks* de la sortie du module de liage (cf.
).

### Méthode d'évaluation

Le résultat de l'exécution de SudoQual est une liste de liens *samesAs*,
*suggestedSameAs* ou *diffFrom*. Pour chaque source, on peut donc
considérer les éléments suivants :

-   un lien *sameAs* obtenu : sameAs<sub>obt</sub>
-   un lien *sameAs* attendu : sameAs<sub>att</sub>
-   un ensemble de liens *suggestedSameAs* obtenu :
    suggestedSameAsSet<sub>obt</sub>
-   un ensemble de liens *suggestedSameAs* attendu :
    suggestedSameAsSet<sub>att</sub>
-   un ensemble de liens *diffFrom* obtenu : diffFromSet<sub>obt</sub>
-   un ensemble de liens *diffFrom* attendu : diffFromSet<sub>att</sub>

L'évaluation à pour but de mesurer la proximité entre les liens obtenus
et les liens attendus. Pour chaque source, on classe le résultat fait
par SudoQual parmi quatre catégories :

-   ****GOOD****, le résultat est conforme aux attentes.
-   ****CAREFUL****, le résultat va dans le sens des attentes.
-   ****UNSATISFACTORY****, le résultat ne satisfait pas les attentes
-   ****BAD****, le résultat est en contradiction avec les attentes.

Les différents cas possibles sont les suivants :

1.  Le diagnostic calculé et le diagnostic attendu contiennent un lien
    sûr
    -   si sameAs<sub>obt</sub> = sameAs<sub>att</sub> alors **GOOD**
    -   sinon sameAs<sub>obt</sub> ≠ sameAs<sub>att</sub> alors **BAD**
2.  Seul le diagnostic calculé contient un lien sûr
    -   si sameAs<sub>obt</sub> ∈ suggestedSameAsSet<sub>att</sub> alors
        **UNSATISFACTORY**
    -   sinon si sameAs<sub>obt</sub> ∉ diffFromSet<sub>att</sub> alors
        **UNSATISFACTORY**
    -   sinon sameAs<sub>obt</sub> ∈ diffFromSet<sub>att</sub> alors
        **BAD**
3.  Seul le diagnostic attendu contient un lien sûr
    -   si sameAs<sub>att</sub> ∈ suggestedSameAsSet<sub>obt</sub> alors
        **CAREFUL**
    -   sinon si sameAs<sub>att</sub> ∉ diffFromSet<sub>obt</sub> alors
        **UNSATISFACTORY**
    -   sinon sameAs<sub>att</sub> ∈ diffFromSet<sub>obt</sub> alors
        **BAD**
4.  Ni le diagnostic calculé, ni le diagnostic attendu ne contiennent de
    lien sûr
    -   si suggestedSameAsSet<sub>obt</sub> =
        suggestedSameAsSet<sub>att</sub> ∧ diffFromSet<sub>obt</sub> =
        diffFromSet<sub>att</sub> alors **GOOD**
    -   sinon si suggestedSameAsSet<sub>obt</sub> ∩
        diffFromSet<sub>att</sub> ≠ ∅ ∨ diffFromSet<sub>obt</sub> ∩
        suggestedSameAsSet<sub>att</sub> ≠ ∅ alors **UNSATISFACTORY**
    -   sinon si suggestedSameAsSet<sub>obt</sub> ⊆
        suggestedSameAsSet<sub>att</sub> ∧ diffFromSet<sub>obt</sub> ⊆
        diffFromSet<sub>att</sub> alors **CAREFUL**
    -   sinon suggestedSameAsSet<sub>obt</sub> ∩
        diffFromSet<sub>att</sub> = ∅ ∧ diffFromSet<sub>obt</sub> ∩
        suggestedSameAsSet<sub>att</sub> = ∅ alors **UNSATISFACTORY**

# Spécifications et architecture (informaticien)

## Moteur de règles

### Instanciation et exécution

``` java
Reader dlpFileReader; // must be a java.io.Reader over a rule file.
Collection<Predicate> predicates; // must contains a set of predicates.

RuleEngine engine = null;
try {
  engine = RuleEngine.create(dlpFileReader, predicates);
} catch (RuleEngineException e) {
  // catch exceptions
}

PredicateManager manager; // must contains data for each feature and a map of computedFeature
Reference source; // must be a reference known by the manager
Reference target; // must be a reference known by the manager

RuleEngine.Result resSameAs = engine.check(manager, "sameAs", source, target);
RuleEngine.Result resDiffFrom = engine.check(manager, "diffFrom", source, target);
```

La méthode `check` est *thread safe*, elle peut donc être appelée
plusieurs fois de façon parallèle.

### Interface du moteur de règle

``` java
interface RuleEngine {
  RuleEngineResult check(FeatureManager data, String queryAtom, Reference from, 
          Reference to) throws RuleEngineException;
  RuleEngineResult check(FeatureManager data, String queryAtom, Reference from, 
          Reference to, int minThreshold) throws RuleEngineException;
  static String getVersion();
}

interface FeatureManager  {
  /**
   * Gets specified feature values as JsonObject. If the specified
   * features contain computed ones and these are not already computed, they
   * will be computed.
   * @return a JsonObject containing for each feature: feature name as key, and 
   * feature value as value.
   */
  JsonObject getFeatureValues(Reference reference, Set<String> features);
  /**
   * Cleans specified computed features for specified references.
   */
  void cleanComputedFeatures(Set<Reference> references, Set<String> features);
  /**
   * Computes specified computed features that are not already computed for 
   * specified references.
   */
  void computeComputedFeatures(Set<Reference> references, Set<String> features);
}

interface Reference {
  /**
   * Gets the reference name.
   */
  public String getName();
}
```

### Grammaire formelle des règles

La syntaxe des règles acceptées correspond à une restriction de la
syntaxe `Dlgp v2` (<http://graphik-team.github.io/graal/doc/dlgp>).
Voici cette grammaire restreinte (cf. [notation
utilisée](https://www.w3.org/TR/REC-xml/#sec-notation)) :

### Filtres, Critères et dimensions

Dans notre utilisation du moteur de règle, chaque prédicat doit
correspondre à une classe Java représentant un *critère*, un *filtre*
(sauf pour les dimensions qui n'ont pas besoin d'être préalablement
déclarées). Ils devront implémenter des interfaces Java spécifiques
définis ci-dessous.

1.  Interface Filtre

    ``` java
    interface Filter {
      String getKey();
      /**
       * Set of needed features to be able to evaluate the filter
       * @return a Set of needed features
       */
      Set<String> featureSet();
      /**
       * The evaluation function
       * @param data a JsonObject containing all needed features
       * @return true if the features fulfill the filter requirement, false otherwise.
       */
      boolean check(JSONObject data);
    }
    ```

2.  Interface Critère

    ``` java
    interface Criterion {
      String getKey();
      /**
       * Get a DiscretCompType representing the co-domain of this
       * criterion.
       * @return a DiscretCompType representing the co-domain
       */
      DiscretCompType getComparisonType();
      /**
       * Set of needed features of the source reference given 
       * as a parameter to be able to evaluate the filter
       * @return a Set of needed features
       */
      Set<String> sourceFeatureSet();
      /**
       * Set of needed features of the target reference given 
       * as a parameter to be able to evaluate the filter
       * @return a Set of needed features
       */
      Set<String> targetFeatureSet();
      /**
       * The evaluation function.
       * @param source a JsonObject containing all needed features the first reference
       * @param target a JsonObject containing all needed features the second reference
       * @return an integer consistent with the DiscretCompType attached to this criterion.
       */
      int compare(JSONObject source, JSONObject target);
    }
    ```

3.  Interface DiscretCompType

    ``` java
    interface DiscretCompType {
      /**
       * Gets the max acceptable value.
       */
      int getMaxValue();
      /**
       * Gets the min acceptable value.
       */
      int getMinValue();
      /**
       * Checks if the given value is an acceptable value for this discretCompType
       * @param value the value to check
       * @return true if the given value is an acceptable value, false otherwise.
       */
      boolean check(int value);
      /**
       * Checks if the given String value is an acceptable value for this discretCompType
       * @param value the String value to check
       * @return true if the given value is an acceptable value, false otherwise.
       */
      boolean check(String value);

      String toString(int value) throws RuleEngineException;

      int toInt(String value) throws RuleEngineException;
    }
    ```

4.  Gestion des dimensions

    Pour respecter le fait qu'un atome de type *dimension*
    "`dim(X,Y,I)`" (avec I un indice entier) est évalué `VRAI` si et
    seulement si un atome de même dimension "`dim(X,Y,J)`" (avec J \>= I
    si I \> 0 ; avec J = 0 si I = 0 ; et avec J \<= I si I \< 0 )
    peut-être déduit, un ensemble de règles est généré automatiquement à
    partir du seuil min et du seuil max trouvé dans le fichier de règle
    pour chaque dimension.

### Fonctionnement (Backend *LBGEngine*)

*LBGEngine* est un moteur de règle *datalog* dont les possibilité sont
restreinte pour coller au besoin. Nous présenterons dans cette partie
sont fonctionnement.

Prenons par exemple le jeu de règles simplifiées suivant :

<div class="captioned-content">

<div class="caption">

Exemple 1

</div>

``` text
[R1] dim1 :- crit1, crit2.
[R2] dim1 :- crit3, crit4.
[R3] dim2 :- dim1, crit5.
[R4] sameAs(a) :- dim2, crit6.
[R5] sameAs(b) :- dim2, crit7.
```

</div>

On souhaite pouvoir répondre à la requête `sameAs?` de façon efficace.
Pour cela, on *compile* l'ensemble des règles en une structure appelée
[*AOCPoset*](http://www.lirmm.fr/AOC-poset-Builder/) (Sous-hiérarchie de
Galois).

1.  Création de l'AOCPoset associé

    Tout d'abord on calcul toutes les combinaisons de règles possibles
    pour répondre à `sameAs`. Ce qui nous donne l'ensemble ci-dessous.

    <div class="captioned-content">

    <div class="caption">

    Ensemble des combinaisons de règles possibles

    </div>

    ``` text
    sameAs(a) = crit1 crit2 crit5 crit6
    sameAs(a) = crit3 crit4 crit5 crit6
    sameAs(b) = crit1 crit2 crit5 crit7
    sameAs(b) = crit3 crit4 crit5 crit7
    ```

    </div>

    Puis on construit la matrice associée à cet ensemble.

    |            | crit1 | crit2 | crit3 | crit4 | crit5 | crit6 | crit7 |
    |------------|-------|-------|-------|-------|-------|-------|-------|
    | sameAs(a1) | x     | x     |       |       | x     | x     |       |
    | sameAs(a2) |       |       | x     | x     | x     | x     |       |
    | sameAs(b1) | x     | x     |       |       | x     |       | x     |
    | sameAs(b2) |       |       | x     | x     | x     |       | x     |

    Matrice Atomes/Critères

    Une fois cette matrice calculé, on utilise une librairie externe
    (`fr.lirmm.marel.gsh2`) qui nous fournit un `AOCPosetBuilder` (voir
    <http://www.lirmm.fr/AOC-poset-Builder/>). On obtient la structure
    suivante :

![AOCPoset de l’exemple 1](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/image-044.png)

    TODO pourquoi pas texte normal ?
    Pour répondre à la requête `sameAs?`, il ne nous reste plus qu'à
    parcourir ce graphe de haut en bas (ici, il n'y a qu'un seul nœud
    possible pour débuter la descente mais c'est un cas particulier).
    Une descente correspond à un `ET` logique, si plusieurs descente
    sont possible cela correspond à un `ET
    (A OU B)`. Lorsque l'on rencontre un critère pour la première fois
    on l'évalue. Si un critère n'est pas validé on remonte. Si tous les
    critères d'un nœud sont validé on descend. Si on atteint un nœud
    contenant le prédicat interrogé la réponse est *vrai*.

2.  Reprenons avec un exemple légèrement plus complexe

    ``` text
    [R1] dim1 :- crit1, crit2.
    [R2] dim1 :- crit3, crit4.

    [R3] dim2 :- dim1, crit5.

    [R4] sameAs(a) :- dim2, crit6.
    [R5] sameAs(b) :- dim2, crit7.
    [R6] sameAs(c) :- crit7, crit8.
    [R7] sameAs(d) :- crit9. 
    ```

![AOCPoset de l’exemple 2](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/image-045.png)

    Dans cette exemple, on peut choisir de débuter la descente par le
    nœud "a1 a2 b1 b2", le nœud "b1 b2 c" ou le nœud "d".

## Module de liage

### Instanciation et exécution

Une fois un scénario de SudoQual configuré, il nous reste à appeler le
module et lui passer les paramètres d'entrée d'une exécution. Et
finalement, en comprendre la sortie.

``` java
LinkingModule module = new LinkingModuleImpl(nbThreads);
String result = module.execute(inputJSON);
```

La méthode `execute` est *thread safe*, elle peut donc être appelée
plusieurs fois de façon parallèle.

### Interface

``` java
interface LinkingModule extends Closeable {
  /**
   * Register a path where LinkingModule will look for scenario files.
   * @param path
   */
  void registerPath(String path);

  /**
   * Executes the linking module over the given input.
   * @param input a String representation of the JSON input 
   * @return a String that represents the output of the module
   * @throws LinkingModuleException
   * @throws InterruptedException
   */
  String execute(String input) throws LinkingModuleException, InterruptedException;

  /**
   * Executes the linking module over the given input.
   * @param input the input as a {@link JSONObject}
   * @return the output of the module as a {@link JSONObject}
   * @throws LinkingModuleException
   * @throws InterruptedException
   */
  JSONObject execute(JSONObject input) throws LinkingModuleException, InterruptedException;

  /**
   * Executes the linking module over the given input.
   * @param input a String representation of the JSON input given as an {@link InputStream}.
   * @param charset the charset to be used to read the input
   * @return a String that represents the output of the module
   * @throws LinkingModuleException
   * @throws InterruptedException
   */
  String execute(InputStream input, Charset charset) throws LinkingModuleException, InterruptedException;

  @Override
  void close();
}
```

### Spécification entrée/sortie

1.  Spécification de l'entrée

    *\[OPT\] signifie que le champ est optionnel.*

    -   **scenario** : un scenario configuré (cf. ),
    -   **sources** : une liste de référence sources,
    -   **targets** : une liste de référence cibles ou une référence
        vers l'ensemble source afin d'utiliser le même ensemble pour
        ensemble source et ensemble cible,
    -   \[OPT\] **supports** : une liste de référence supports ou une
        référence vers l'ensemble source ou l'ensemble cible afin
        d'utiliser ce même ensemble comme ensemble support.
    -   \[OPT\] **initialLinks** : une liste de liens initiaux (utilisé
        pour produire un attribut spécial "initialLink" pour chaque
        *source* ou *target* référencé dans un élément de cette liste) :
        -   **type** : type du lien *sameAs* ou *diffFrom*,
        -   **source** : référence source,
        -   **target** : référence cible ;
    -   \[OPT\] **safeLinks** : une liste de liens sûrs :
        -   **type** : type du lien *sameAs* ou *diffFrom*,
        -   **source** : référence source,
        -   **target** : référence cible ;
    -   **features** : un ensemble d'feature pour chaque référence
        **source**, **cible** et **support**,
    -   \[OPT\] **criterionValues** : un ensemble de valeurs de filtres
        et de critères à utiliser comme résultat de l'évaluation de
        celui-ci pour la référence ou le couple de référence spécifiés,
    -   \[OPT\] **options** : une liste d'options (ces options écrasent
        les valeurs par défaut définies par le scenario):
        -   \[OPT\] **validatedSameAsThreshold** : seuil à partir duquel
            un lien "sameAs" est validé ,
        -   \[OPT\] **suggestedSameAsThreshold** : seuil à partir duquel
            un lien "sameAs" est suggéré,
        -   \[OPT\] **validatedDiffFromThreshold** : seuil à partir
            duquel un lien "diffFrom" est validé,
        -   \[OPT\] **suggestedEnabled** : activation/désactivation des
            suggestions,
        -   \[OPT\] **keepOnlyBestSuggestions** : si activé, conserve
            seulement le résultat de *confidence* la plus élevée, sinon
            la totalité des suggestions trouvées seront retournées,
        -   \[OPT\] **dataValidationEnabled** : activation/désactivation
            de la vérification des données avant traitement,
        -   \[OPT\] **exportCriterionValues** : activation/désactivation
            de l'export de la valeur des filtres et critères évalués
            durant l'exécution de SudoQual (pour réutilisation/mise en
            cache dans un mode interactif),
        -   \[OPT\] **debug** : activation du mode "debug" (retourne un
            ensemble d'information supplémentaire concernant l'exécution
            du traitement dans le JSON de sortie).

    Les entrées seront fournit au module de liage selon le format
    ci-dessous (exprimé en [JSON
    Schema](http://json-schema.org/specification.html)). De plus, les
    données fournies doivent être cohérentes, c'est à dire que :

    -   le scenario spécifié doit être un nom de scenario valide ;
    -   chaque **source** référencée par un **initialLink** doit
        également être présente dans le tableau **sources** ;
    -   chaque **target** référencée par un **initialLink** doit
        également être présente dans le tableau **targets** ;
    -   un même couple **source**, **target** ne peut être présent dans
        deux **initialLink** différents.
    -   chaque **source** référencée par un **safeLink** doit également
        être présente dans le tableau **sources** ou **supports** ;
    -   chaque **target** référencée par un **safeLink** doit également
        être présente dans le tableau **targets** ou **supports** ;
    -   un même couple **source**, **target** ne peut être présent dans
        deux **safeLink** différents.

    ``` json
    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "title": "linking module input",
      "type": "object",
      "additionalProperties": false,
      "required": [
        "scenario",
        "sources",
        "targets",
        "features"
      ],
      "properties": {
        "scenario": {"type": "string"},
        "options": {
          "type": "object",
          "additionalProperties": false,
          "properties": {
             "debug": {"type": "boolean"},
            "validatedSameAsThreshold": {"type": "integer"},
            "suggestedSameAsThreshold": {"type": "integer"},
            "validatedDiffFromThreshold": {"type": "integer"},
            "suggestedEnabled": {"type": "boolean"},
            "keepOnlyBestSuggestions": {"type": "boolean"},
            "exportCriterionValues": {"type": "boolean"},
            "dataValidationEnabled": {"type": "boolean"}
          }
        },
        "sources": {
          "type": "array",
          "items": {"type": "string"},
          "minItems": 1,
          "uniqueItems": true
        },
        "targets": {
          "oneOf" : [
            {
              "type": "array",
              "items": {"type": "string"},
              "minItems": 1,
              "uniqueItems": true
            },
            {
              "enum": ["sources"]
            }
          ]
        },
        "supports": {
          "oneOf" : [
            {
              "type": "array",
              "items": {"type": "string"},
              "minItems": 0,
              "uniqueItems": true
            },
            {
              "enum": ["sources", "targets"]
            }
           ]
        },
        "safeLinks": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "type": {"type": "string"},
              "source": {"type": "string"},
              "target": {"type": "string"}
            },
            "required": [
              "type",
              "source",
              "target"
            ],
            "additionalProperties": false
          }
        },
        "features": {
          "type": "object"
        },
        "criterionValues": {
          "type": "array",
          "items": {
            "oneOf" : [
              {
                "type": "object",
                "properties": {
                  "name": {"type": "string"},
                  "source": {"type": "string"},
                  "target": {"type": "string"},
                  "value": {"type": "integer"}
                },
                "required": [
                  "name",
                  "source",
                  "target",
                  "value"
                ],
                "additionalProperties": false
              },
              {
                "type": "object",
                "properties": {
                  "name": {"type": "string"},
                  "reference": {"type": "string"},
                  "value": {"type": "boolean"}
                },
                "required": [
                  "name",
                  "reference",
                  "value"
                ],
                "additionalProperties": false
              }
            ]
          }
        },
        "initialLinks": {
        "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "type": {
                "enum": [
                  "sameAs",
                  "diffFrom"
                ]
              },
              "source": {
                "type": "string"
              },
              "target": {
                "type": "string"
              }
            },
            "required": [
              "type",
              "source",
              "target"
            ],
            "additionalProperties": false
          }
        }
      }
    }
    ```

2.  Spécification de la sortie

    *\[OPT\] signifie que le champ est optionnel.*

    -   **computedLinks** : une liste de lien :
        -   **source** : référence source,
        -   **target** : référence cible,
        -   **type** : sameAs/suggestedSameAs/diffFrom,
        -   **why** : information sur les éléments ayant permis ce lien,
        -   **confidence** : indice de confiance (valeur du sameAs ou
            diffFrom dans la règle déclenchée),
        -   \[OPT\] : information sur l'étape à laquelle le lien a été
            calculé (mode debug uniquement) ;
    -   **metadata** : des informations sur la configuration de
        l'exécution de SudoQual qui a produit cette sortie :
        -   **version** : version de SudoQual utilisée,
        -   **scenario** : scenario utilisé,
        -   \[OPT\] **options** : liste des options utilisées et leur
            valeur ;
    -   \[OPT\] **debug** : informations diverses (mode debug
        uniquement).

    La sortie du module de liage correspond au format ci-dessous
    (exprimé en [JSON
    Schema](http://json-schema.org/specification.html)). Le module
    fournit au maximum un lien par couple (source, target).

    ``` json
    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "title": "linking module output",
      "type": "object",
      "additionalProperties": false,
      "required": [
        "metadata",
        "computedLinks"
      ],
      "properties": {
        "metadata": {
          "type": "object",
          "additionalProperties": false,
          "required": [
            "version",
            "scenario"
          ],
          "properties": {
            "version": {"type": "string"},
            "scenario": {"type": "string"},
            "options": {
              "type": "object",
              "additionalProperties": true
              }
            }
          }
        },
        "computedLinks": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "type": {"enum": ["sameAs", "suggestedSameAs", "diffFrom"]},
              "source": {"type": "string"},
              "target": {"type": "string"},
              "why": {
                "type": "object",
              },
              "confidence": {"anyOf": [{"type": "integer"},{"type": "string"}]},
              "step": {"type": "integer"}
            },
            "required": [
              "type",
              "source",
              "target",
              "confidence"
            ],
            "additionalProperties": false
          }
        },
        "criterionValues": {
          "type": "array",
          "items": {
            "oneOf" : [
              {
                "type": "object",
                "properties": {
                  "name": {"type": "string"},
                  "source": {"type": "string"},
                  "target": {"type": "string"},
                  "value": {"type": "integer"}
                },
                "required": [
                  "name",
                  "source",
                  "target",
                  "value"
                ],
                "additionalProperties": false
              },
              {
                "type": "object",
                "properties": {
                  "name": {"type": "string"},
                  "reference": {"type": "string"},
                  "value": {"type": "boolean"}
                },
                "required": [
                  "name",
                  "reference",
                  "value"
                ],
                "additionalProperties": false
              }
            ]
          }
        },
        "debug": {"type": "object"}
      }
    }
    ```

### Architecture

![](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/image-046.png)

### Attributs (`Feature`)

Un attributs doit implémenter l'interface `Feature` suivante :

``` java
interface Feature {

    /**
     * Gets the feature key.
     * @return the feature key
     */
    String getKey();

}
```

La clé permettant d'y faire référence dans les données d'entrées ou dans
les filtres et critères doit être un identifiant unique. Elle est
définie par la méthode `getKey`.

**Les clés « uri » et « initialLink » sont réservés pour des attributs
spéciaux:**

-   La clé **uri** représentera un attribut de type « chaîne de
    caractère » représentant l'identifiant de la référence courante.
-   La clé **initialLink** représentera un attribut de type « tableau de
    chaîne de caractère » représentant les identifiants des références
    initialement liés par un `sameAs` à la référence courante.

Pour définir un attribut de type *pré-traité* ou *calculé*, il faut
implémenter l'une des sous interfaces suivantes :

1.  L'interface `PreprocessedFeature`

    offre la possibilité de pré-traité la donnée d'entrée brut avant son
    utilisation par le module de liage.

    ``` java
    interface PreprocessedFeature<FROM, TO> extends Feature {

        /**
         * Builds the final value based on the provided raw value. The final value 
         * will be used by filters and criterions.
         * @param rawValue
         * @return the final value.
         */
        TO buildValue(FROM rawValue);
    }
    ```

2.  L'interface `ComputedFeature`

    offre la possibilité de faire évoluer la valeur de l'attributs en
    cours d'exécution à partir des liens sûrs calculés par le module de
    liage. Les attributs, déclarés par la méthodes `getRelatedFeatures`,
    des références déduites comme `sameAs` de façon sûr, seront fournit
    en entrée de la méthode `compute` (un `JSONObject` par référence)
    pour le calcul de la valeur courante de cette attribut.

    ``` java
    interface ComputedFeature<T> extends Feature {

        /**
         * Gets feature names that are needed to compute this one.
         * @return a set of needed feature names.
         */
        Collection<String> getRelatedFeatures();

        /**
         * The computation function
         * @param json 
         * @param store
         * @return the computed value of this feature
         */
        T compute(Collection<JSONObject> selectedData);
    }
    ```

3.  L'interface `UpdateableComputedFeature`

    est similaire à l'interface `ComputedFeature` mais offre la
    possibilité de mettre à jour la valeur précédemment calculée tandis
    que l'utilisation de l'interface `ComputedFeature` nécessite de
    recalculé la valeur en repartant de zéro.

    ``` java
    interface UpdateableComputedFeature<T> extends ComputedFeature<T> {

        /**
         * Update the old computed value with the new data from newly related 
         * references given by the parameter newlySelectedData.
         * @param oldValue
         * @param newlySelectedData
         * @return the computed value of this feature
         */
        T update(T oldValue, Collection<JSONObject> newlySelectedData);

    }
    ```

### Algorithmes et implémentation

1.  boucle principale (main loop)

    La classe `LinkingModuleAlgorithmImpl` implémente la boucle
    principale de l'application. Cette boucle principale crée des tâches
    de liage qui seront traité en parallèle par un ensemble de
    *consumers* (cf. section ).

2.  Modèle producer/consumer

    Le modèle de parallélisation utilisé est un modèle
    "producer/consumer". Dans ce modèle une `BlockingQueue` est partagée
    entre un ensemble de "Producers" et un ensemble de "Consumers".
    Chaque "Consumer" est exécuté dans un Thread dédié. Les "producers"
    créent des tâches qu'ils insèrent dans la `BlockingQueue`. Les
    "Consumers" restent en écoute sur la `BlockingQueue` jusqu'à
    obtention d'une tâche. Une fois qu'un "consumer" a obtenu une tâche,
    il la traite et la marque comme "done" puis il se remet en attente
    d'une nouvelle tâche.

![Modèle Producers/Consumers](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/image-047.png)

    TODO pourquoi pas texte normal ci-dessous ?
    Pour l'application de ce modèle à SudoQual, deux types de tâche ont
    été implémentées :

    UpdateComputedFeatureTask,  
    pour une référence et une liste d'attributs calculés, cette tâche
    met à jour chacun des attributs de la liste pour cette référence.

    LinkingTask,  
    pour une référence source et une liste de références cibles, cette
    tâche appelle le moteur de règles (méthode `check`) pour trouver le
    plus grand *sameAs* et le plus grand *diffFrom* puis en fait
    l'aggrégation  
    (`heuristic.computeProximity`) appelée *indice de proximité*. Enfin,
    elle passe l'ensemble des couples *\<cible, indice de proximité\>* à
    l'heuristic (`findLinks`).

3.  Heuristic de liage

    Une « heuristic de liage » implémente l'interface suivante :

    ``` java
    interface LinkHeuristic {

      /**
       * Defines the name of this heuristic
       * @return a String representing the name of this heuristic
       */
      String getKey();

      /**
       * The main method which will provide links between the specified source and
       * specified targets.
       * 
       * @param store a PredicateManager to evaluate predicates and features.
       * @param engine a RuleEngine to provides co-reference clues.
       * @param source the reference to link
       * @param allCandidates candidates references to be linked with the source
       * @param currentStep the current step of the main loop
       * @param isCancelled true if the task was cancelled, so the method should 
       * throw a CanceledTaskException as soon as possible. False, otherwise.
       * @return a collection of Link for the specified source.
       * @throws LinkHeuristicException
       * @throws CanceledTaskException 
       */
      Collection<Link> findLinks(PredicateManager store, RuleEngine engine, Reference source,
          Set<Candidate> allCandidates, AtomicBoolean isCancelled)
          throws LinkHeuristicException, CanceledTaskException;

      /**
       * Gets the current heuristic mode.
       * @return the current heuristic mode.
       */
      HeuristicMode getMode();

      /**
       * Sets the heuristic mode.
       * @param mode a String representing the name of the heuristic mode to set to. If the name is 
       * not recognize, this method will throw an UnsupportedHeuristicModeException.
       * @throws UnsupportedHeuristicModeException 
       */
      void setMode(String mode) throws UnsupportedHeuristicModeException;

      /**
       * Allows to configure the heuristic.
       * @param properties ConfigurationProperties representing the main configuration file
       * @throws LinkHeuristicException
       */
      void configure(ConfigurationProperties properties) throws LinkHeuristicException;

      /**
       * Aggregates weight from sameAs rule and diffFrom rule.
       * @param source
       * @param sameAsClue
       * @param target
       * @param diffFromClue
       * @return a positive value if the aggregation should be interpreted as a co-reference clue or 
       * a negative value if it should be interpreted as a non co-reference clue.
       */
      int computeProximity(Reference source, Result sameAsClue, Reference target, Result diffFromClue);

      /**
       * Check if a target does not appear in two sameAs link.
       * This treatment cannot be called directly by the heuristic because it processes sources independently.
       * @param newLinks
       * @return
       */
      Set<Link> checkAndHandleOneToNSameAsConflict(Set<Link> actualLinks);
    }
    ```

    La méthode `findLinks` est la première à être appelée pour une
    source et un ensemble de cibles. L'ensemble de cibles est fournit
    sous la forme d'une liste de « Candidate » qui contiennent chacun
    l'information de la valeur du *sameAs* et *diffFrom* les plus forts
    produit par le moteur de règles. En retour, cette méthode doit
    fournir une liste de *sameAs*, *suggestedSameAs* ou *diffFrom*
    effectifs (ils seront validés définitivement).

    Pour ce faire, cette méthode peut s'appuyer sur la méthode
    `computeProximity` qui à partir de l'indice de *sameAs* et de
    *diffFrom*, calcule un indice de proximité prenant en compte ces
    deux informations. Mais également, sur la méthode
    `checkAndHandleOneToNSameAsConflict` pour traiter les liens en cas
    de conflit sur la contrainte "one-to-**". Les conflits "**-to-one"
    seront traités après l'exécution de la méthode `findLinks` par la
    boucle principale.

    {{{SudoQual}}} fournit une implémentation de cette interface
    (`BasicLinkHeuristic`). Elle est référencée avec la clé "default".
    Cette implémentation fonctionne avec trois seuils (sur l'indice de
    proximité) à partir duquel les liens sont déclarés *sameAs*,
    *suggestedSameAs* et *diffFrom*. De plus cette implémentation ne
    conserve que les liens *sameAs* d'indice de proximité le plus fort.
    C'est à dire que si deux liens sont candidats en tant que *sameAs*,
    l'un avec un indice de proximité 5 et l'autre 6 (tous deux supérieur
    ou égal au seuil *sameAs*), alors seul le candidat d'indice 6 sera
    validé en tant que *sameAs*, l'autre sera degradé en
    *suggestedSameAs*. Finalement, si il y a conflit sur la contrainte
    "one-to-\*", plusieurs candidats de même indices maximums, alors ils
    seront tous dégradés en *suggestedSameAs* par la méthode
    `checkAndHandleOneToNSameAsConflict`.

4.  Chargement des scénarios et jeux de règles

    Les fichiers de scénarios (.properties) et de jeux de règles (.dlp)
    doivent se trouver dans un chemin référencé auprès du module de
    liage (`LinkingModule`) à travers la méthode `registerPath`. Ces
    fichiers sont chargé à travers la méthode
    `LinkingModule.class.getResource` Cette dernière méthode est
    initialisée avec le dossier "*scenarios*".

5.  Chargement des attributs/filtres/critères

    Le chargement des attributs, filtres, critères et heuristiques se
    fait via la classe `BusinessClassLoader`. Celle-ci utilise
    l'introspection
    (<https://fr.wikipedia.org/wiki/R%C3%A9flexion_(informatique)>). à
    travers la librairie "org.reflections"
    (<https://github.com/ronmamo/reflections>) pour trouver toutes les
    classes implémentant l'interface `Predicate`, `Feature` ou
    `LinkHeuristic` dans un `package` spécifié. Puis elle conserve une
    `Map` permettant de retrouver une classe particulière à partir du
    nom de l'attribut, du critère, du filtre ou de l'heuristic *(le nom
    est définit par la méthode `getName()` des interfaces `Predicate`,
    `Feature` et `LinkHeuristic`)*.

6.  ExportCriterionValues (Cache)

    L'activation de l'option `exportCriterionValues` permet une
    éventuelle mise en cache de la valeur des critères calculés lors
    d'une exécution de SudoQual en vue d'une réutilisation ultérieur,
    par exemple, dans un cas d'utilisation interactif (*Paprika*).

7.  Système de LOG

    Utilisation de "slf4j-api" avec l'implémentation "logback".

## Autres modules

### module « diagnostic »

Le module de diagnostic implémente actuellement un diagnostic pour le
mode de liage "many-to-one". Il pourrait être étendu pour pouvoir
diagnostiquer les autres modes.

1.  Instanciation et exécution

    ``` java
    JSONObject input; // must contains a valid input
    JSONObject result = null;
    try {
      result = Diagnostician.createManyToOneDiagnostician().execute(input);
    } catch (DiagnosticianException e) {
      // catch exceptions
    }
    ```

2.  Interface

    ``` java
    interface Diagnostician {
      String execute(String input) throws DiagnosticianException;
      String execute(InputStream input, Charset charset) throws DiagnosticianException;
      JSONObject execute(JSONObject input) throws DiagnosticianException;
    }
    ```

3.  Spécification des entrées/sorties

    1.  Spécification de l'entrée

        L'entrée de ce module est compatible avec la sortie du module de
        liage TODO 2 page 76.

        ``` json
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "title": "diagnostic input",
          "type": "object",
          "properties": {
            "sources": {
              "type": "array",
              "items": {
                "type": "string"
              }
            },
            "targets": {
              "type": "array",
              "items": {
                "type": "string"
              }
            },
            "initialLinks": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "type": {
                    "enum": [
                      "sameAs",
                      "diffFrom"
                    ]
                  },
                  "source": {
                    "type": "string"
                  },
                  "target": {
                    "type": "string"
                  }
                },
                "required": [
                  "type",
                  "source",
                  "target"
                ]
              }
            },
            "computedLinks": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "type": {
                    "enum": [
                      "sameAs",
                      "suggestedSameAs",
                      "diffFrom"
                    ]
                  },
                  "source": {
                    "type": "string"
                  },
                  "target": {
                    "type": "string"
                  }
                },
                "required": [
                  "type",
                  "source",
                  "target"
                ]
              }
            }
          },
          "required": [
            "sources",
            "targets",
            "initialLinks",
            "computedLinks"
          ],
          "additionalProperties": false
        }
        ```

    2.  Spécification de la sortie

        ``` json
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "title": "diagnostic output",
          "type": "object",
          "definitions": {
            "targetWithWhy": {
              "type": "object",
              "properties": {
                "target": {
                  "type": "string"
                },
                "why": {
                  "type": "object"
                }
              },
              "required": [
                "target"
              ]
            }
          },
          "properties": {
            "diagnostic": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "source": {
                    "type": "string"
                  },
                  "initialLink": {
                    "type": "string"
                  },
                  "computedLink": {
                    "$ref": "#/definitions/targetWithWhy"
                  },
                  "suggestedLinks": {
                    "type": "array",
                    "items": {
                      "$ref": "#/definitions/targetWithWhy"
                    }
                  },
                  "impossibleLinks": {
                    "type": "array",
                    "items": {
                      "$ref": "#/definitions/targetWithWhy"
                    }
                  },
                  "case": {
                    "type": "integer",
                    "minimum": 1,
                    "maximum": 12
                  },
                  "status": {
                    "enum": [
                      "validatedLink",
                      "doubtfulLink",
                      "missingLink",
                      "almostValidatedLink",
                      "erroneousLink"
                    ]
                  }
                },
                "required": [
                  "source",
                  "case",
                  "status"
                ],
                "additionalProperties": false
              }
            }
          },
          "required": [
            "diagnostic"
          ],
          "additionalProperties": false
        }
        ```

### module « clustering »

Le module de « clustering » permet de transformer une liste de liens
produit par le module de liage (configuré en many-to-many et
source=target) en une liste de clusters.

1.  Instanciation et exécution

    ``` java
    JSONObject input; // must contains a valid input
    JSONObject result = null;
    try {
      result = Clustering.process(input);
    } catch (Exception e) {
      // catch exceptions
    }
    ```

2.  Spécification des entrées/sorties

    1.  Spécification de l'entrée

        L'entrée de ce module est compatible avec la sortie du module de
        liage TODO 2 page 76.

        ``` json
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "title": "clustering input",
          "type": "object",
          "properties": {
            "sources": {
              "type": "array",
              "items": {
                "type": "string"
              }
            },
            "targets": {
              "type": "array",
              "items": {
                "type": "string"
              }
            },
            "initialLinks": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "type": {
                    "enum": [
                      "sameAs",
                      "diffFrom"
                    ]
                  },
                  "source": {
                    "type": "string"
                  },
                  "target": {
                    "type": "string"
                  }
                },
                "required": [
                  "type",
                  "source",
                  "target"
                ]
              }
            },
            "computedLinks": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "type": {
                    "enum": [
                      "sameAs",
                      "suggestedSameAs",
                      "diffFrom"
                    ]
                  },
                  "source": {
                    "type": "string"
                  },
                  "target": {
                    "type": "string"
                  },
                  "why": {
                    "type": "array",
                    "items": {
                      "type": "string"
                    }
                  }
                },
                "required": [
                  "type",
                  "source",
                  "target"
                ]
              }
            }
          },
          "required": [
            "sources",
            "targets",
            "initialLinks",
            "computedLinks"
          ],
          "additionalProperties": false
        }
        ```

    2.  Spécification de la sortie

        ``` json
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "title": "clustering output",
          "type": "object",
          "properties": {
            "clusters": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "type": {"enum": ["sameAs", "suggestedSameAs", "diffFrom"]},
                  "source": {"type": "string"},
                  "target": {"type": "string"}
                },
                "required": [
                  "type",
                  "source",
                  "target"
                ],
                "additionalProperties": true
              }
            }
          },
          "required": [
            "clusters"
          ],
          "additionalProperties": false
        }
        ```

### module « constraint »

Le module de contraintes permet de filtrer un ensemble de liens « sameAs
» en fonction des contraintes suivantes : « one-to-one », « one-to-many
», « many-to-one ». Les éléments ne respectant pas la contrainte
spécifiée seront rétrogradées en tant que « suggestedSameAs ». Il est
possible de spécifier un ensemble de liens « sûrs » qui ne pourront pas
être rétrogradés mais qui déclencheront potentiellement le rétrogradage
d'autres liens.

1.  Entrées

    -   un ensemble de liens
    -   un ensemble de liens sûrs

### module « cluster-ra-overlap »

À partir de trois ensembles :

-   un ensemble de rc
-   un ensemble d'ensembles de rc (ra)
-   un autre ensemble d'ensembles de rc (cluster)

Ce module permet de trouver toute les ra qui sont partiellement
recouvertes par un cluster (c'est à dire que au moins X% des rc
rattachées à cette ra appartiennent à un même cluster)

1.  comportement

    -   si 1 cluster (C1) recouvre en partie (\>= X%) (suggestedSameAs
        si \>= Y%) les RCs reliées à une RA (RA1) alors on veut sortir
        le lien (C1 sameAs/suggestedSameAs RA1).
TODO : reformuler ou supprimer 
    1.  cas à discuter (non implémentés)

        (on peut le faire par le module de transitivité) cas1  
        1 cluster contient 1 lien validé

        cas3?  
        2 à n clusters recouvre en partie (\>= X%) plusieurs targets //
        traiter à part ? cas 1% - 99% + 99% - 1%, il faudrait une règle
        pour que la répartition soit assez homogène (si déséquilibre =\>
        plutôt erreur de lien). Par exemple, il faudrait que chaque RA
        soit recouverte par au moins 30% du cluster.

2.  Entrée

    -   liens RC/RA

    -   liens RC/Clusters

    -   seuil sameAs (X) (0 - 100)

    -   seuil suggestedSameAs (Y) (-1 - 100), "-1" = désactivé

    ``` json
    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "additionalProperties": true,
      "required": [
        "links",
        "clusters"
      ],
      "properties": {
         "links": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "type": {
                "enum": [
                  "sameAs",
                  "suggestedSameAs",
                  "diffFrom"
                ]
              },
              "source": {
                "type": "string"
              },
              "target": {
                "type": "string"
              }
            },
            "required": [
              "type",
              "source",
              "target"
            ],
            "additionalProperties": true
          }
        },
         "clusters": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "type": {
                "enum": [
                  "sameAs",
                  "suggestedSameAs"
                ]
              },
              "source": {
                "type": "string"
              },
              "cluster": {
                "type": "string"
              }
            },
            "required": [
              "type",
              "source",
              "target"
            ],
            "additionalProperties": true
          }
        }
      }
    }
    ```

3.  Exemple

    ``` json
    {
       "clusters":[
          {
             "source":"sudoc:234881038-1",
             "type":"sameAs",
             "cluster":"_:cluster1"
          },
          {
             "source":"sudoc:234882352-1",
             "type":"sameAs",
             "cluster":"_:cluster1"
          }
       ],
       "links":[
          {
             "source":"sudoc:234881828-2",
             "type":"sameAs",
             "target":"idref:23488035X"
          },
          {
             "source":"sudoc:234881038-1",
             "type":"sameAs",
             "target":"idref:23488035X"
          },
          {
             "source":"sudoc:234882352-1",
             "type":"sameAs",
             "target":"idref:23488035X"
          }
       ]
    }
    ```

4.  Sortie

    -   liste de liens RA/Cluster (sameAs ou SuggestedSameAs)

    ``` json
    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "additionalProperties": false,
      "required": [
        "computedLinks",
      ],
      "properties": {
        "computedLinks": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "type": {
                "enum": [
                  "sameAs",
                  "suggestedSameAs",
                ]
              },
              "source": {
                "type": "string"
              },
              "cluster": {
                "type": "string"
              },
              "why": {
                "type": "object"
              }
            },
            "required": [
              "type",
              "source",
              "target",
            ],
            "additionalProperties": false
          }
        },
      }
    }
    ```

5.  Exemples

    ``` json
    {
      "computedLinks": [{
        "cluster": "_:cluster1",
        "type": "sameAs",
        "target": "idref:23488035X"
      }]
    }
    ```

### module « inconsistences »

Le module d'incohérences permet de vérifier la cohérences d'un ensemble
de liens *sameAs* et *diffFrom*. Ce module n'est actuellement pas
spécifié.

1.  Exemple de format d'entrée actuel

    ``` json
    {
       "computedLinks":[
         {
             "source":"sudoc:Z",
             "type":"sameAs",
             "target":"idref:W"
          },
          {
             "source":"sudoc:A",
             "type":"sameAs",
             "target":"idref:B"
          },
          {
             "cluster":"_:C1",
             "type":"sameAs",
             "target":"idref:C"
          },
          {
             "source":"sudoc:A",
             "type":"diffFrom",
             "target":"idref:C"
          },
          {
             "source":"sudoc:A",
             "type":"sameAs",
             "cluster":"_:C1"
          }
       ]
    }
    ```

2.  Exemple de format de sortie actuel

    ``` json
    {
      "inconsistencies": [
        [
          {
            "cluster": "_:C1",
            "type": "sameAs",
            "target": "idref:C"
          },
          {
            "source": "sudoc:A",
            "type": "diffFrom",
            "target": "idref:C"
          },
          {
            "cluster": "_:C1",
            "source": "sudoc:A",
            "type": "sameAs"
          }
        ]
      ],
      "computedLinks": [
        {
          "source": "sudoc:Z",
          "type": "sameAs",
          "target": "idref:W"
        },
        {
          "source": "sudoc:A",
          "type": "sameAs",
          "target": "idref:B"
        },
        {
          "cluster": "_:C1",
          "type": "suggestedSameAs",
          "target": "idref:C"
        },
        {
          "cluster": "_:C1",
          "source": "sudoc:A",
          "type": "suggestedSameAs"
        }
      ]
    }
    ```

### module « transitivity »

Le module de « transitivity » produit à partir d'une liste de liens
*sameAs*, un ensemble de nouveau liens *sameAs* par application de la
transitivité des liens *sameAs*. Les liens produits respectent une
contrainte sur le type de références liées.

1.  Entrée

    **links** 
    liste de *sameAs*

    -   source/target
    -   source/cluster
    -   cluster/target

    -   looksForLinks :: typologie de liens rechercher
        -   from \[source ou target ou cluster\]
        -   to \[ sourc ou target ou cluster\]
        -   constraint \[one-to-one, many-to-one, one-to-many,
            many-to-many\]

    ``` json
    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "definitions": {
        "reference": {
          "type": "string"
        },
        "links": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "type": {
                "enum": [
                  "sameAs",
                  "suggestedSameAs",
                  "diffFrom"
                ]
              },
              "source": {
                "$ref": "#/definitions/reference"
              },
              "target": {
                "$ref": "#/definitions/reference"
              },
              "cluster": {
                "$ref": "#/definitions/reference"
              }
            },
            "oneOf": [
              {
                "required": [
                  "type",
                  "source",
                  "target"
                ]
              },
              {
                "required": [
                  "type",
                  "source",
                  "cluster"
                ]
              },
              {
                "required": [
                  "type",
                  "target",
                  "cluster"
                ]
              },
            ],
            "additionalProperties": false
          }
        }
      },
      "additionalProperties": true,
      "properties": {
        "links": {
          "$ref": "#/definitions/links"
        },
        "looksForLinks": {
          "type": "array",
          "items": {
            "type": "object",
            "additionalProperties": false,
            "properties": {
              "from": {
                "enum": ["source", "target", "cluster"]
              },
              "to": {
                "enum": ["source", "target", "cluster"]
              }
            }
          }
        }
      }
    }
    ```

2.  Sortie

    un ensemble de liens *sameAs* produit (disjoint de l'ensemble
    d'entrée)

    **computedLinks**  
    ensemble de liens calculés

    -   oneOf
        -   targets - array de RA
        -   sources - array de RC
        -   clusters - array de clusters
        -   source + target
        -   source + cluster
        -   target + cluster
    -   type - sameAs/suggestedSameAs

    ``` json
    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "definitions": {
        "reference": {
          "type": "string"
        },
        "group": {
          "type": "array",
          "items": {
            "$ref": "#/definitions/reference"
          }
        }
      },
      "additionalProperties": false,
      "properties": {
        "computedLinks": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "targets": {
                "$ref": "#/definitions/group"
              },
               "sources": {
                "$ref": "#/definitions/group"
              },
               "clusters": {
                "$ref": "#/definitions/group"
              },
              "source": {
                "$ref": "#/definitions/reference"
              },
              "target": {
                "$ref": "#/definitions/reference"
              },
              "cluster": {
                "$ref": "#/definitions/reference"
              },
              "type": {
                "enum": [
                  "sameAs",
                  "suggestedSameAs",
                  "diffFrom"
                ]
              }
            },
            "oneOf": [
              {
                "required": [
                  "type",
                  "source",
                  "target"
                ]
              },
              {
                "required": [
                  "type",
                  "source",
                  "cluster"
                ]
              },
              {
                "required": [
                  "type",
                  "target",
                  "cluster"
                ]
              },
              {
                "required": [
                  "type",
                  "targets",
                ]
              },
               {
                "required": [
                  "type",
                  "clusters"
                ]
              },
               {
                "required": [
                  "type",
                  "sources",
                ]
              },
            ],
            "additionalProperties": false,
          }
        }
      }
    }
    ```

3.  comportement

    on a 3 types de références:

    -   source
    -   target
    -   cluster

    On crée un lien entre une référence A et une référence B

    -   ssi il existe un chemin entre ces deux références en ne passant
        que par des liens sameAs
    -   ET si le type de A et le type de B correspondent à une typologie
        de lien recherchée tel que définie par la propriété
        "looksForLink(from,to)"
    -   ET si le lien n'était pas présent en entrée

4.  exemple:

    à partir d'un lien "a sameAs b" et d'un lien "b sameAs c" produit "a
    sameAs c".

5.  Important:

    le 19 septembre en présence en discussion avec Yann Nicolas et Aline
    Le Provost, il a été décidé que si ce module venait à engendrer
    des violations de contraintes (RC/RA many-to-one) alors les liens
    sûrs en entrée ne doivent pas être remis en cause et les nouveaux
    liens générés doivent être downgradé en "suggestedSameAs".

    -   

## Services

Seul les services les plus compliqués sont détaillés ici.

### service « complete »

1.  Spécification de l'entrée

    ``` json
    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "title": "linking module input",
      "type": "object",
      "definitions": {
        "sub-scenario": {
          "type": "object",
          "supports": {
            "oneOf" : [
              {
                "type": "array",
                "items": {"type": "string"},
                "minItems": 0,
                "uniqueItems": true
              },
              {
                "enum": ["sources", "targets"]
              }
            ]
          },
          "properties": {
            "scenario": {"type": "string"},
            "options": {
              "type": "object",
              "additionalProperties": false,
              "properties": {
                "validatedSameAsThreshold": {"type": "integer"},
                "suggestedSameAsThreshold": {"type": "integer"},
                "validatedDiffFromThreshold": {"type": "integer"},
                "suggestedEnabled": {"type": "boolean"},
                "keepOnlyBestSuggestions": {"type": "boolean"},
                "dataValidationEnabled": {"type": "boolean"}
              }
            }
          },
          "safeLinks": {
            "type": "array",
            "items": {
              "type": "object",
              "properties": {
                "type": {"type": "string"},
                "source": {"type": "string"},
                "target": {"type": "string"}
              },
              "required": [
                "type",
                "source",
                "target"
              ],
              "additionalProperties": false
            }
          },
        }
      },
      "additionalProperties": false,
      "required": [
        "scenario",
        "sources",
        "targets",
        "features"
      ],
      "properties": {
        "sources-sources": {
          "$ref" : "#/definitions/sub-scenario"
        },
        "sources-targets": {
          "$ref" : "#/definitions/sub-scenario"
        },
        "targets-targets": {
          "$ref" : "#/definitions/sub-scenario"
        },
        "sources": {
          "type": "array",
          "items": {"type": "string"},
          "minItems": 1,
          "uniqueItems": true
        },
        "targets": {
          "oneOf" : [
            {
              "type": "array",
              "items": {"type": "string"},
              "minItems": 1,
              "uniqueItems": true
            },
            {
              "enum": ["sources"]
            }
          ]
        },
        "features": {
          "type": "object"
        },
        "initialLinks": {
        "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "type": {
                "enum": [
                  "sameAs",
                  "diffFrom"
                ]
              },
              "source": {
                "type": "string"
              },
              "target": {
                "type": "string"
              }
            },
            "required": [
              "type",
              "source",
              "target"
            ],
            "additionalProperties": false
          }
        }
      }
    }
    ```

    1.  Exemple

        ``` json
        {
          "sources": [ 
            "sudoc:a",
            "sudoc:b",
            "sudoc:c"
          ],
          "targets" : [
            "idref:1", 
            "idref:2", 
            "idref:3"
          ],
          "initialLinks": [
            {
              "source": "sudoc:a", 
              "type": "sameAs", 
              "target": "idref:1"
            }
          ],
          "features": {
            "sudoc:a": {},
            "sudoc:b": {},
            "sudoc:c": {},
            "idref:1": {},
            "idref:2": {},
            "idref:3": {}
          },
          "sources-sources" : {
            "scenario": "sudoc-rc-rc"
          },
          "sources-targets" : {
            "scenario": "sudoc-rc-ra",
            "supports": "sources",
            "safeLinks": []
          },
          "targets-targets" : {
            "scenario": "sudoc-ra-ra",
            "options": {}
          }
        }
        ```

2.  Spécification de la sortie

    ``` json
    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "title": "linking module output",
      "type": "object",
      "definitions" : {
        "computedLinks" : {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "type": {"enum": ["sameAs", "suggestedSameAs", "diffFrom"]},
              "source": {"type": "string"},
              "target": {"type": "string"},
              "why": {
                "type": "object",
              },
              "confidence": {"anyOf": [{"type": "integer"},{"type": "string"}]},
              "step": {"type": "integer"}
            },
            "required": [
              "type",
              "source",
              "target",
              "confidence"
            ],
            "additionalProperties": false
          }
        },
        "targetWithWhy": {
          "type": "object",
          "properties": {
            "target": {
              "type": "string"
            },
            "why": {
              "type": "object"
            }
          },
          "required": [
            "target"
          ]
        }
      },
      "additionalProperties": false,
      "required": [
        "metadata",
        "computedLinks"
      ],
      "properties": {
        "metadata": {
          "type": "object",
          "additionalProperties": false,
          "required": [
            "version",
            "scenario"
          ],
          "properties": {
            "version": {"type": "string"},
            "scenario": {"type": "string"},
            "options": {
              "type": "object",
              "additionalProperties": true
              }
            }
          }
        },
        "clusters": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "type": {"enum": ["sameAs", "suggestedSameAs", "diffFrom"]},
              "source": {"type": "string"},
              "target": {"type": "string"}
            },
            "required": [
              "type",
              "source",
              "target"
            ],
            "additionalProperties": true
          }
        },
        "diagnosticSources" : {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "source": {
                "type": "string"
              },
              "initialLink": {
                "type": "string"
              },
              "computedLink": {
                "$ref": "#/definitions/targetWithWhy"
              },
              "suggestedLinks": {
                "type": "array",
                "items": {
                  "$ref": "#/definitions/targetWithWhy"
                }
              },
              "impossibleLinks": {
                "type": "array",
                "items": {
                  "$ref": "#/definitions/targetWithWhy"
                }
              },
              "case": {
                "type": "integer",
                "minimum": 1,
                "maximum": 12
              },
              "status": {
                "enum": [
                  "validatedLink",
                  "doubtfulLink",
                  "missingLink",
                  "almostValidatedLink",
                  "erroneousLink"
                ]
              }
            },
            "required": [
              "source",
              "case",
              "status"
            ],
            "additionalProperties": false
          }
        },
        "diagnosticTargets" : {},
        "computedLinks-sources-sources": {
          "$ref" : "#/dedfinitions/computedLinks"
        },
        "computedLinks-sources-targets": {
          "$ref" : "#/dedfinitions/computedLinks"
        },
        "computedLinks-targets-targets": {
          "$ref" : "#/dedfinitions/computedLinks"
        }
      }
    }
    ```

    1.  Exemple

        ``` json
        {
          "clusters": [],
          "diagnosticSources" : [
          ],
          "diagnosticTargets": [
            {
              "targets": ["idref:1", "idref:2"],
              "type": "sameAs",
              "why": {}
            }
          ],
          "computedLinks-sources-sources": [],
          "computedLinks-sources-targets": [],
          "computedLinks-targets-targets": [],
          "inconsistencies": []
        }
        ```

3.  Implémentation visée

![Architecture visée du service « complete »](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/image-048.png)       

4.  Implémentation actuelle

![Architecture actuelle du service « complete »](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/image-049.png) 

### service « align »

1.  Implémentation visée

![Architecture visée du service « align »](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/image-050.png) 

2.  Implémentation actuelle

![Architecture actuelle du service « align »](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/image-051.png) 

## Client Batch (CLI)

Le client batch utilise la librairie **JCommander**
<http://www.jcommander.org/> pour la gestion des arguments (ligne de
commande). Celui-ci doit être instancier pour chaque "configuration de
SudoQual". Par exemple, le configuration "linking-scenarios-sudoc"
contient le code suivant permettant l'instantiation du CLI.

``` java
package fr.abes.sudoqual.sudoc;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import fr.abes.sudoqual.cli.SudoqualCLI;
import fr.abes.sudoqual.cli.SudoqualConfig;

public final class SudocConfig implements SudoqualConfig {
  private static String SCENARIO_DIR = "/fr/abes/sudoqual/sudoqual1/scenarios/";

  @Override
  public String getScenarioDir() {
    return SCENARIO_DIR;
  }

  @Override
  public Charset getCharset() {
    return StandardCharsets.UTF_8;
  }

  public static void main(String args[]) {
    SudoqualCLI.run(new SudocConfig(), args);
  }
}
```

La classe ci-dessus permettra la génération du CLI pour cette
configuration lors du "build" de celle-ci avec Maven. Cette génération
est déclarée dans le fichier "pom.xml" via le code ci-dessous :

``` xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <version>3.2.1</version>
  <executions>
    <!-- Run shade goal on package phase -->
    <execution>
      <phase>package</phase>
      <goals>
        <goal>shade</goal>
      </goals>
      <configuration>
        <finalName>sudoqual-cli</finalName>
        <transformers>
          <!-- add Main-Class to manifest file -->
          <transformer
            implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
            <mainClass>fr.abes.sudoqual.sudoc.SudocConfig</mainClass>
            <manifestEntries>
              <Built-On>${maven.build.timestamp}</Built-On>
              <Specification-Vendor>Clément SIPIETER</Specification-Vendor>
              <Specification-Title>${project.name}</Specification-Title>
              <Specification-Version>${project.version}</Specification-Version>
              <JDK-Target-Version>${maven.compiler.target}</JDK-Target-Version>
            </manifestEntries>
          </transformer>
        </transformers>
      </configuration>
    </execution>
  </executions>
</plugin>
```

L'implémentation du CLI utilise le système de « command » de JCommander
<http://www.jcommander.org/#_more_complex_syntaxes_commands>. Voici la
liste des commandes existantes :

**link**  
appel le module de liage

**diagnostic**  
appel le module de diagnostic

**compare**  
compare deux sorties du module de liage

**eval**  
exécute et évalue un fichier de benchmark (cf. TODO page 65)

## Client Lourd (RichClient)

Le client lourd se présente sous la forme d'un plugin Eclipse. Les
différents "points d'extension" utilisés sont les suivants :

**org.eclipse.core.resources.natures**  
permet d'associé une "nature Qualinka" au projet ouvert, et donc
d'activer les fonctionnalités du module.

**org.eclipse.core.expressions.propertyTester** 
permet de vérifier certaines propriétés sur des fichiers (type de
fichier json, java… ? ; interface implémentée par les fichiers Java…)

**org.eclipse.ui.commands**  
permet d'implémenter une commande pour l'ajout/suppression de la "nature
Qualinka" au projet courant.

**org.eclipse.ui.menus**  
permet de référencer la commande ci-dessus dans un menu.

**org.eclipse.ui.propertyPages**  
permet de proposer une page de configuration des propriétés du module
pour le projet courant (cf. TODO 6.4.3 page 51)

**org.eclipse.core.runtime.preferences**  
permet de sauvegarder les préférences.

**org.eclipse.ui.navigator.navigatorContent**  
permet de personnaliser le contenu du "project explorer".

**org.eclipse.debug.ui.launchShortcuts**  
permet l'ajout d'entrées dans le menu "run as \>".

**org.eclipse.ui.perspectives**  
permet d'implémenter une "perspective Qualinka".

La partie "éditeur de règles" s'appuie sur le framework `xtext`
<https://www.eclipse.org/Xtext/>. Il est premièrement définit par un
fichier de grammaire (`src/fr/abes/sudoqual/dlgp.xtext` dans le
sous-projet fr.abes.sudoqual.dlgp). Ce fichier définit la syntaxe
autorisé pour les fichiers de règles, en voici le contenu :

``` text
grammar fr.abes.sudoqual.Dlgp hidden(WS, ML_COMMENT)

import "http://www.eclipse.org/emf/2002/Ecore" as ecore

generate dlgp "http://www.abes.fr/sudoqual/Dlgp"

Document:
  (statements+=Rule '.')*
;

Rule:
  ('[' label=Label ']')? head=HeadAtom ':-' body=Conjunction 
;

Conjunction:
  atoms+=Atom (',' atoms+=Atom)*
;

Atom:
  (dimension=[DimensionPredicate|DIM_IDENT] | predicate=Predicate) '(' terms+=Term (',' terms+=Term)* ')'
;

HeadAtom:
  (dimension=DimensionPredicate | goal=GoalPredicate) '(' terms+=Term (',' terms+=Term)* ')'
;

GoalPredicate:
  name=SAME_AS | name=DIFF_FROM
;

DimensionPredicate:
  name=DIM_IDENT
;

Predicate:
  name=LIDENT
;

Term:
  Variable | Constant | Literal
;

Variable:
  label=UIDENT
;

Constant:
  label=LIDENT
;

Literal:
  label=STRING | value=INTEGER
;

Label:
  (TEXT | LIDENT | UIDENT)+
;

// DataType

terminal STRING:
    '"' ( '\\' . /* 'b'|'t'|'n'|'f'|'r'|'u'|'"'|"'"|'\\' */ | !('\\'|'"') )* '"' |
    "'" ( '\\' . /* 'b'|'t'|'n'|'f'|'r'|'u'|'"'|"'"|'\\' */ | !('\\'|"'") )* "'"
;

terminal INTEGER returns ecore::EInt: 
  ('+'|'-')?('0'..'9')+
;

// Identifiers

terminal UIDENT:
  ('A'..'Z') ('a'..'z' | 'A'..'Z' | '0'..'9' | '_')*;

terminal DIFF_FROM:
  'diffFrom'
;

terminal SAME_AS:
  'sameAs'
;

terminal DIM_IDENT: 
// terminal LIDENT:
  // the name ID is important
  'dim_' ('a'..'z') ('a'..'z' | 'A'..'Z' | '0'..'9' | '_')*
;

terminal LIDENT: 
  ('a'..'z') ('a'..'z' | 'A'..'Z' | '0'..'9' | '_')*
;

terminal TEXT:
  ('a'..'z' | 'A'..'Z' | '0'..'9' | '_' | '-')+
;

// hidden

terminal ML_COMMENT:
  // Single line comment using ML_COMMENT terminal (Multi-Line)  to
  // support folding
    ('%' !('\n'|'\r')* ('\r'? '\n'))*
    '%' !('\n'|'\r')* ('\r'? '\n')?
;

terminal WS: 
  (' '|'\t'|'\r'|'\n')+
;
```

La classe `DlgpValidator` permet une validation sémantique du fichier.

Dans le sous projet `fr.abes.sudoqual.dlgp.ui`,

**DlgpUiModule** permet de déclarer les classes ci-dessous auprès du module.

**DlgpHighlightingConfiguration** permet d'enrichir la coloration syntaxique.

**DlgpDocumentationProvider** permet d'afficher la documentation d'un
    filtre ou critère au survol.
**DlgpFoldingRegionProvider** permet de masquer (replier) les
    commentaires.
**DlgpProposalProvider** permet de gérer l'autocomplétion

## Web Services

### Architecture

Le diagramme de classes figure TODO page suivante, décrit l'architecture des web services.

![Architecture des Web Services](https://raw.githubusercontent.com/abes-esr/sudoqual-framework/develop/documentation/images/image-052.png)

### Paramétrage du module

Le module de web services peut-être configuré via le fichier
"application.properties" se trouvant dans le dossier
"src/main/resources". Les paramètres suivant sont disponible :

-   **service.results.ttl**: durée en minutes pendant laquelle le
    résultat d'une exécution doit être conservée.
-   **service.path.results**: chemin pour l'accès aux résultats des
    exécutions, ce chemin est relatif au chemin principale de
    l'application.
-   **service.path.inputs**: chemin pour l'accès aux données d'entrée
    des exécutions, ce chemin est relatif au chemin principale de
    l'application.
-   **service.path.services**: chemin pour l'accès aux services associés
    aux exécutions, ce chemin est relatif au chemin principale de
    l'application.
-   **service.path.jobs**: chemin pour l'accès aux informations sur les
    exécutions en cours, ce chemin est relatif au chemin principale de
    l'application.
-   **service.linking_module.nbThreads**: permet de fixer le nombre de
    threads utilisé par le module de liage. Par défaut ce nombre est
    obtenu dynamiquement est correspond au résultat de la méthode Java
    "Runtime.getRuntime().availableProcessors()".
-   **service.linking_module.scenarios**: permet de déclarer les
    scénarios disponible pour le module de liage. les différents
    scénarios doivent être séparés par un ";", exemple:
    "scenario-a;scenario-b;scenario-c".

### Notes de déploiement

L'application de web services SudoQual est prévue pour être déployée sur
un serveur Tomcat \>= 9 exécuté sous une JVM \>= 11.

La machine hôte doit posséder au moins 4Go de RAM et un processeur avec
au moins 4 threads. Il est conseillé pour un environnement de production
une machine avec au moins 8Go de RAM et un processeur avec au moins 16
threads. Ces paramètres peuvent varier en fonction du nombre
d'exécutions de "jobs" simultanées.

# Perspectives (techniques)

## Amélioration

-   \[DONE\] Faire en sorte que les « web-services » s'appuie sur le CLI
    pour exécuter les différentes fonctions.

-   Modèle de parallélisation avec tâches fines (une source / une cible)
    puis mapping par source (?ToOne) et/ou par cible (oneTo?) pour la
    gestion des contraintes via le module de contraintes récement
    (v2.9.0) introduit (cela pourrait rendre plus propre le traitement
    fait par l'heuristic actuellement fortement orienté manyToOne)

-   Permettre la spécification d'un ensemble de constantes par critère.
    Cela permettrait d'écrire `personName(Source, Target, identical)` ou
    `personName(Source, Target, similar)`

-   Fusion de la notion de filtre et critère au profit d'une notion de
    prédicat plus générique. Un tel prédicat pourrait prendre une ou
    deux référence ainsi qu'une liste de paramètre dont éventuellement
    un seuil. Cela permettrait par exemple d'avoir des critères booléen
    (`sameRole(Source, Target)`) ou des filtres avec paramètre
    (`role(Source, "author")`)… Ou encore `pubDate(RC1, RC2, 7)` qui
    pourrait être vrai si et seulement si les dates de publications des
    deux RC sont distantes d'au plus 7 ans.

TODO manque 10 - Vocabulaire => peut-on faire des renvois ?
