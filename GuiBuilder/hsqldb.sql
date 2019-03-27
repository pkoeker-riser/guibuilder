create table counter
(
    oid                             int                    not null,
    counter                         int                    not null,
    primary key (oid)
);

create cached table adresse
(
    adrsid                          int                    not null,
    kennung                         varchar(50)            not null,
    name1                           varchar(50)            not null,
    name2                           varchar(50)                    ,
    name3                           varchar(50)                    ,
    strasse                         varchar(50)                    ,
    plz                             varchar(10)                    ,
    ort                             varchar(50)                    ,
    anrede                          varchar(50)                    ,
    telefon1                        varchar(50)                    ,
    telefon2                        varchar(50)                    ,
    telefax                         varchar(50)                    ,
    email                           varchar(50)                    ,
    homepage                        varchar(50)                    ,
    privat                          boolean        default false            not null,
    bemerkung                       char(20)                       ,
    primary key (adrsid),
    unique (kennung)
);

create cached table funktionen
(
    funkid                          int                    not null,
    funktion                        varchar(50)            not null,
    primary key (funkid)
);

create cached table schlagworte
(
    slgwid                          int                    not null,
    schlagwort                      varchar(50)            not null,
    primary key (slgwid)
);

create cached table person
(
    persid                          int                    not null,
    fk_adrsid                       int                    not null,
    fk_funkid                       int                            ,
    name                            varchar(50)            not null,
    durchwahl                       varchar(50)                    ,
    privat                          boolean        default false            not null,
    primary key (persid),
    foreign key (fk_adrsid)
       references adresse (adrsid),
    foreign key (fk_funkid)
       references funktionen (funkid)
);

create index persfk on person (fk_adrsid);

create cached table notiz
(
    notizid                         int                    not null,
    fk_adrsid                       int                            ,
    bemerkung                       varchar(255)               not null,
    datum                           datetime                       ,
    erledigt                        boolean     default false               not null,
    primary key (notizid),
    foreign key (fk_adrsid)
       references adresse (adrsid)
);

create index notizfk on notiz (fk_adrsid);

create cached table termin
(
    terminid                        int                    not null,
    fk_adrsid                       int                            ,
    fk_persid                       int                            ,
    datum                           datetime               not null,
    von                             datetime                       ,
    bis                             datetime                       ,
    bemerkung                       varchar(255)                       ,
    primary key (terminid),
    foreign key (fk_adrsid)
       references adresse (adrsid),
    foreign key (fk_persid)
       references person (persid)
);

create index terminfk on termin (fk_adrsid);

create cached table adrsslgw
(
    fk_slgwid                       int                    not null,
    fk_adrsid                       int                    not null,
    primary key (fk_slgwid, fk_adrsid),
    foreign key (fk_adrsid)
       references adresse (adrsid),
    foreign key (fk_slgwid)
       references schlagworte (slgwid)
);

create cached table bestellung
(
    bestellid                       int                    not null,
    fk_adrsid                       int                            ,
    artikel                         varchar(50)                    ,
    menge                           int                            ,
    einzelpreis                     float                          ,
    primary key (bestellid),
    foreign key (fk_adrsid)
       references adresse (adrsid)
);

