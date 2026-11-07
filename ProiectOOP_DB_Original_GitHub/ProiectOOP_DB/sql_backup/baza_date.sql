--
-- PostgreSQL database dump
--

\restrict Aay2OYDfxiv2aDvtf8dS6ZdJrwn062ghthQkzDxf0VeBUXV6lYnp3T8swudubKF

-- Dumped from database version 17.7
-- Dumped by pg_dump version 17.7

-- Started on 2026-01-07 01:20:52

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 239 (class 1255 OID 17118)
-- Name: verifica_permisiune_notare(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.verifica_permisiune_notare() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Verificăm dacă studentul are deja o notă EXACT la materia pe care o predă profesorul curent
    IF EXISTS (
        SELECT 1 FROM public.catalog 
        WHERE id_student = NEW.id_student 
          AND id_materie = NEW.id_materie 
          AND tip_inregistrare = 'Nota'
    ) AND NEW.tip_inregistrare = 'Nota' THEN
        RAISE EXCEPTION 'Studentul are deja o notă la această materie!';
    END IF;

    -- Verificăm dacă anul și limba materiei coincid cu ale studentului (pentru securitate)
    IF NOT EXISTS (
        SELECT 1 FROM public.studenti s
        JOIN grupe g ON s.id_grupa = g.id
        JOIN materie m ON m.id_materie = NEW.id_materie
        WHERE s.id = NEW.id_student 
          AND g.an = m.an 
          AND g.limba = m.limba
    ) THEN
        RAISE EXCEPTION 'Interzis: Materia nu corespunde anului/limbii studentului!';
    END IF;

    RETURN NEW;
END;
$$;


ALTER FUNCTION public.verifica_permisiune_notare() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 227 (class 1259 OID 17202)
-- Name: admini; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.admini (
    id integer NOT NULL,
    nume character varying(100) NOT NULL,
    email character varying(100) NOT NULL,
    parola character varying(100) NOT NULL
);


ALTER TABLE public.admini OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 17201)
-- Name: admini_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.admini_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.admini_id_seq OWNER TO postgres;

--
-- TOC entry 4960 (class 0 OID 0)
-- Dependencies: 226
-- Name: admini_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.admini_id_seq OWNED BY public.admini.id;


--
-- TOC entry 225 (class 1259 OID 17004)
-- Name: catalog; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.catalog (
    id integer NOT NULL,
    id_student integer,
    id_profesor integer,
    id_materie integer,
    tip_inregistrare character varying(20),
    valoare integer,
    data_inregistrare date DEFAULT CURRENT_DATE
);


ALTER TABLE public.catalog OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 17003)
-- Name: catalog_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.catalog_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.catalog_id_seq OWNER TO postgres;

--
-- TOC entry 4961 (class 0 OID 0)
-- Dependencies: 224
-- Name: catalog_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.catalog_id_seq OWNED BY public.catalog.id;


--
-- TOC entry 217 (class 1259 OID 16818)
-- Name: grupe; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.grupe (
    id character varying(10) NOT NULL,
    specializare character varying(50) DEFAULT 'CTI'::character varying,
    numar_grupa integer,
    limba integer,
    an integer
);


ALTER TABLE public.grupe OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16849)
-- Name: materie; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.materie (
    id_materie integer NOT NULL,
    id_profesor integer,
    nume_materie character varying(100),
    limba integer,
    an integer,
    credite integer
);


ALTER TABLE public.materie OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 16848)
-- Name: materie_id_materie_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.materie_id_materie_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.materie_id_materie_seq OWNER TO postgres;

--
-- TOC entry 4962 (class 0 OID 0)
-- Dependencies: 220
-- Name: materie_id_materie_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.materie_id_materie_seq OWNED BY public.materie.id_materie;


--
-- TOC entry 219 (class 1259 OID 16825)
-- Name: profesori; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.profesori (
    id integer NOT NULL,
    email character varying(100) NOT NULL,
    parola character varying(100) NOT NULL,
    nume character varying(50),
    prenume character varying(50),
    limba character varying(50)
);


ALTER TABLE public.profesori OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 16824)
-- Name: profesori_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.profesori_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.profesori_id_seq OWNER TO postgres;

--
-- TOC entry 4963 (class 0 OID 0)
-- Dependencies: 218
-- Name: profesori_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.profesori_id_seq OWNED BY public.profesori.id;


--
-- TOC entry 223 (class 1259 OID 16989)
-- Name: studenti; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.studenti (
    id integer NOT NULL,
    nume character varying(50) NOT NULL,
    prenume character varying(50) NOT NULL,
    email character varying(100) NOT NULL,
    parola character varying(100) NOT NULL,
    id_grupa character varying(10),
    telefon character varying(15) DEFAULT '0744144944'::character varying
);


ALTER TABLE public.studenti OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 16988)
-- Name: studenti_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.studenti_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.studenti_id_seq OWNER TO postgres;

--
-- TOC entry 4964 (class 0 OID 0)
-- Dependencies: 222
-- Name: studenti_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.studenti_id_seq OWNED BY public.studenti.id;


--
-- TOC entry 4774 (class 2604 OID 17205)
-- Name: admini id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.admini ALTER COLUMN id SET DEFAULT nextval('public.admini_id_seq'::regclass);


--
-- TOC entry 4772 (class 2604 OID 17007)
-- Name: catalog id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.catalog ALTER COLUMN id SET DEFAULT nextval('public.catalog_id_seq'::regclass);


--
-- TOC entry 4769 (class 2604 OID 16852)
-- Name: materie id_materie; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.materie ALTER COLUMN id_materie SET DEFAULT nextval('public.materie_id_materie_seq'::regclass);


--
-- TOC entry 4768 (class 2604 OID 16828)
-- Name: profesori id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.profesori ALTER COLUMN id SET DEFAULT nextval('public.profesori_id_seq'::regclass);


--
-- TOC entry 4770 (class 2604 OID 16992)
-- Name: studenti id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.studenti ALTER COLUMN id SET DEFAULT nextval('public.studenti_id_seq'::regclass);


--
-- TOC entry 4954 (class 0 OID 17202)
-- Dependencies: 227
-- Data for Name: admini; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.admini (id, nume, email, parola) FROM stdin;
1	Administrator Sistem	admin@unitbv.ro	admin123
2	Administrator Sistem	user@admin	admin123
\.


--
-- TOC entry 4952 (class 0 OID 17004)
-- Dependencies: 225
-- Data for Name: catalog; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.catalog (id, id_student, id_profesor, id_materie, tip_inregistrare, valoare, data_inregistrare) FROM stdin;
9	345	11	7	Nota	5	2026-01-03
10	358	11	7	Nota	8	2026-01-03
13	345	16	8	Nota	10	2026-01-03
14	358	16	8	Nota	10	2026-01-03
16	345	17	10	Nota	7	2026-01-03
17	358	17	10	Nota	7	2026-01-03
19	345	13	9	Nota	6	2026-01-03
20	358	13	9	Nota	6	2026-01-03
22	361	16	8	Nota	10	2026-01-03
23	361	13	9	Nota	3	2026-01-03
25	361	11	7	Nota	3	2026-01-03
26	361	17	10	Nota	7	2026-01-03
27	343	17	10	Nota	7	2026-01-03
31	364	11	7	Nota	6	2026-01-04
32	345	15	6	Nota	4	2026-01-05
33	358	15	6	Nota	5	2026-01-05
34	361	15	6	Nota	4	2026-01-05
35	345	15	6	Absenta	0	2026-01-05
36	345	15	6	Absenta	0	2026-01-06
40	357	11	7	Nota	3	2026-01-05
41	451	11	7	Nota	5	2026-01-05
42	25	11	2	Nota	5	2026-01-05
45	226	11	2	Nota	4	2026-01-05
46	249	11	2	Nota	6	2026-01-05
48	343	15	6	Absenta	0	2026-01-06
50	132	11	2	Nota	5	2026-01-06
51	339	15	6	Nota	7	2026-01-06
52	476	15	6	Nota	0	2026-01-06
53	25	11	2	Absenta	0	2026-01-06
47	23	11	2	Nota	4	2026-01-05
55	16	11	2	Nota	3	2026-01-06
60	351	11	7	Absenta	0	2026-01-06
\.


--
-- TOC entry 4944 (class 0 OID 16818)
-- Dependencies: 217
-- Data for Name: grupe; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.grupe (id, specializare, numar_grupa, limba, an) FROM stdin;
2111	CTI	1	1	1
2121	CTI	1	1	2
2131	CTI	1	1	3
2141	CTI	1	1	4
2112	CTI	2	1	1
2122	CTI	2	1	2
2132	CTI	2	1	3
2142	CTI	2	1	4
2113	CTI	3	1	1
2123	CTI	3	1	2
2133	CTI	3	1	3
2143	CTI	3	1	4
2211	CTI	1	2	1
2221	CTI	1	2	2
2231	CTI	1	2	3
2241	CTI	1	2	4
2212	CTI	2	2	1
2222	CTI	2	2	2
2232	CTI	2	2	3
2242	CTI	2	2	4
\.


--
-- TOC entry 4948 (class 0 OID 16849)
-- Dependencies: 221
-- Data for Name: materie; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.materie (id_materie, id_profesor, nume_materie, limba, an, credite) FROM stdin;
7	11	Special Matematics	2	1	6
8	16	Computer Programming	2	1	7
9	13	Electrotechnics	2	1	6
10	17	Differential Calculus	2	1	5
16	25	Fundamental Algorithms	2	2	7
17	16	Object Oriented Programming	2	2	6
18	17	Numerical Methods	2	2	5
19	26	Computer Arhitecture	2	2	7
20	24	Data Bases	2	2	5
26	25	Logic Programming	2	3	6
27	29	Intelligent Systems	2	3	6
28	30	Computer Networks	2	3	6
29	27	Artificial Intelligence	2	3	6
30	31	Design with Microprocessors	2	3	6
36	27	Systems	2	4	6
37	16	Security	2	4	7
38	21	Expert Systems	2	4	6
39	33	Database Design	2	4	5
40	34	Operating System Design	2	4	6
1	10	Proiectare Logica	1	1	6
2	11	Matematici Speciale	1	1	6
4	13	Electrotehnica	1	1	6
5	14	Calcul Diferential	1	1	5
11	20	Algoritmi Fundamentali	1	2	7
12	21	Programare Orientata Obiect	1	2	6
13	22	Metode Numerice	1	2	5
14	23	Arhitectura Calculatoarelor	1	2	7
15	24	Baze de Date	1	2	5
21	20	Programare Logica	1	3	6
22	27	Sisteme Inteligente	1	3	6
23	28	Retele de calculatoare	1	3	6
24	29	Inteligenta artificiala	1	3	6
25	26	Proiectare cu Microprocesare	1	3	6
31	27	Sisteme	1	4	6
32	16	Securitate	1	4	7
33	29	Sisteme experte	1	4	6
34	24	Proiectare Baze Date	1	4	5
35	32	Administrarea Sistemelor de Operare	1	4	6
6	15	Logic Design	2	1	6
3	12	Programarea Calculatoarelor	1	1	7
\.


--
-- TOC entry 4946 (class 0 OID 16825)
-- Dependencies: 219
-- Data for Name: profesori; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.profesori (id, email, parola, nume, prenume, limba) FROM stdin;
10	dragos.lisman@teacher	123	Lisman	Dragos	romana
14	mircea.rus@teacher	123	Rus	Mircea	romana
20	camelia.lemnaru@teacher	123	Lemnaru	Camelia	romana
22	ioan.gavrea@teacher	123	Gavrea	Ioan	romana
23	florin.oniga@teacher	123	Oniga	Florin	romana
28	bogdan.iancu@teacher	123	Iancu	Bogdan	romana
32	ciprian.oprisa@teacher	123	Oprisa	Ciprian	romana
17	mircea.ivan@teacher	123	Ivan	Mircea	engleza
25	rodica.potolea@teacher	123	Potolea	Rodica	engleza
30	vlad.dadarlat@teacher	123	Dadarlat	Vlad	engleza
31	radu.danescu@teacher	123	Danescu	Radu	engleza
33	mircea.antal@teacher	123	Antal	Mircea	engleza
34	adrian.colesa@teacher	123	Colesa	Adrian	engleza
11	daniela.rosca@teacher	123	Rosca	Daniela	romana/engleza
13	laura.darabant@teacher	123	Darabant	Laura	romana/engleza
16	marius.joldos@teacher	123	Joldos	Marius	romana/engleza
21	ion.giosan@teacher	123	Giosan	Ion	romana/engleza
24	calin.cenan@teacher	123	Cenan	Calin	romana/engleza
26	mircea.negru@teacher	123	Negru	Mircea	romana/engleza
27	adrian.groza@teacher	123	Groza	Adrian	romana/engleza
29	radu.slavescu@teacher	123	Slavescu	Radu	romana/engleza
12	robert.varga@teacher	123	Varga	Robert	romana
15	octavian.cret@teacher	you_will_cry_at_the_exam	Cret	Octavian	engleza
\.


--
-- TOC entry 4950 (class 0 OID 16989)
-- Dependencies: 223
-- Data for Name: studenti; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.studenti (id, nume, prenume, email, parola, id_grupa, telefon) FROM stdin;
1	Ciutan	Diana	diana.ciutan@student	123	2111	0766372400
2	Cheta	Teodora	teodora.cheta@student	123	2111	0746376266
3	Mozacu	Marius	marius.mozacu@student	123	2111	0721788205
5	Ionescu	Gabriela	gabriela.ionescu@student	123	2111	0736833449
6	Popovici	Ioana	ioana.popovici@student	123	2111	0763204015
7	Eminescu	Adrian	adrian.eminescu@student	123	2111	0727394496
8	Conea	Andrei	andrei.conea@student	123	2111	0730722034
9	Traian	Cristian	cristian.traian@student	123	2111	0766604565
10	Gherasim	Catalin	catalin.gherasim@student	123	2111	0743053588
11	Pop	Claudiu	claudiu.pop@student	123	2111	0725314383
12	Barbu	Adina	adina.barbu@student	123	2111	0727208223
13	Bucur	Andreea	andreea.bucur@student	123	2111	0759514542
14	Grec	Raluca	raluca.grec@student	123	2111	0796845385
15	Hambasan	Gratiana	gratiana.hambasan@student	123	2111	0765013018
16	Balea	Traian	traian.balea@student	123	2111	0724804837
17	Ortan	Adrian	adrian.ortan@student	123	2111	0770812317
18	Coman	Patricia	patricia.coman@student	123	2111	0795000789
19	Gheorghe	Diana	diana.gheorghe@student	123	2111	0755147540
20	Farcas	Raluca	raluca.farcas@student	123	2111	0775570798
21	Muresan	Andreea	andreea.muresan@student	123	2111	0769771207
22	Ciutan	Teodora	teodora.ciutan@student	123	2111	0748855855
23	Balaj	Ionut	ionut.balaj@student	123	2111	0719910436
24	Popa	Cristina	cristina.popa@student	123	2111	0792381399
25	Balaj	Ioana	ioana.balaj@student	123	2111	0757423111
26	Popa	Claudia	claudia.popa@student	123	2111	0781070697
27	Lauriuc	Gabriela	gabriela.lauriuc@student	123	2111	0718377296
28	Stoica	Gabriela	gabriela.stoica@student	123	2111	0727471807
29	Munteanu	Radu	radu.munteanu@student	123	2111	0717013201
30	Harghel	Oana	oana.harghel@student	123	2121	0734942503
31	Sabou	Cristian	cristian.sabou@student	123	2121	0752508643
32	Lauriuc	Cristian	cristian.lauriuc@student	123	2121	0766008900
34	Contra	Denisa	denisa.contra@student	123	2121	0770899447
35	Iacob	Catinca	catinca.iacob@student	123	2121	0756043736
36	Cretu	Raluca	raluca.cretu@student	123	2121	0791770640
37	Apostu	Iulia	iulia.apostu@student	123	2121	0751667095
38	Budnar	Gratiana	gratiana.budnar@student	123	2121	0752753711
39	Bolojan	Gratiana	gratiana.bolojan@student	123	2121	0712784087
40	Stan	Camelia	camelia.stan@student	123	2121	0729769962
41	Soponar	Cristina	cristina.soponar@student	123	2121	0733375580
42	Marc	Denis	denis.marc@student	123	2121	0788218834
43	Sandu	Nicolas	nicolas.sandu@student	123	2121	0722262619
44	Berindan	Mihai	mihai.berindan@student	123	2121	0717722185
45	Gheorghe	Traian	traian.gheorghe@student	123	2121	0781210155
46	Sumalan	Oana	oana.sumalan@student	123	2121	0745648080
47	Sumalan	Cristian	cristian.sumalan@student	123	2121	0728668661
48	Hambasan	Nicoleta	nicoleta.hambasan@student	123	2121	0715534150
49	Sabou	Nicoleta	nicoleta.sabou@student	123	2121	0795687582
50	Cucura	Stefan	stefan.cucura@student	123	2121	0714786355
51	Nistor	Mircea	mircea.nistor@student	123	2121	0735081671
52	Moiceanu	Marin	marin.moiceanu@student	123	2121	0731002454
53	Barbu	Patricia	patricia.barbu@student	123	2121	0789150284
54	Sabou	Ioana	ioana.sabou@student	123	2121	0719837731
55	Blidar	Emanuel	emanuel.blidar@student	123	2121	0759663497
56	Rus	Ionut	ionut.rus@student	123	2121	0797291785
57	Munteanu	Gratiana	gratiana.munteanu@student	123	2121	0724640274
58	Dobra	Mihaela	mihaela.dobra@student	123	2121	0791475939
59	Boangher	Ionela	ionela.boangher@student	123	2131	0776139522
60	Iancu	Denis	denis.iancu@student	123	2131	0715736238
61	Sandu	Serban	serban.sandu@student	123	2131	0781402005
62	Gheorghe	Ioana	ioana.gheorghe@student	123	2131	0781697331
63	Ionescu	Lucas	lucas.ionescu@student	123	2131	0782671768
65	Bontos	Radu	radu.bontos@student	123	2131	0752397035
66	Boesan	Claudia	claudia.boesan@student	123	2131	0761841294
67	Talpos	Miruna	miruna.talpos@student	123	2131	0715968560
68	Iliasa	Nicoleta	nicoleta.iliasa@student	123	2131	0772253282
69	Grec	Elena	elena.grec@student	123	2131	0716868646
70	Iacob	Patricia	patricia.iacob@student	123	2131	0755657559
71	Grec	Valentin	valentin.grec@student	123	2131	0799741947
72	Barbu	Nicoleta	nicoleta.barbu@student	123	2131	0796673087
73	Moiceanu	Patricia	patricia.moiceanu@student	123	2131	0771918452
74	Radu	Thomas	thomas.radu@student	123	2131	0730944495
75	Conea	Stefania	stefania.conea@student	123	2131	0732340855
76	Balaj	Patricia	patricia.balaj@student	123	2131	0716587244
77	Cornici	Octavian	octavian.cornici@student	123	2131	0798633327
78	Ghemes	Iulia	iulia.ghemes@student	123	2131	0783421867
79	Adam	Iulia	iulia.adam@student	123	2131	0769885615
80	Gheorghe	Adrian	adrian.gheorghe@student	123	2131	0763642129
81	Craciun	Iulian	iulian.craciun@student	123	2131	0742415179
82	Bolovan	Adriana	adriana.bolovan@student	123	2131	0733623431
83	Sandu	Florentina	florentina.sandu@student	123	2131	0763249130
84	Comsa	Octavian	octavian.comsa@student	123	2131	0790940433
85	Cheta	Iulia	iulia.cheta@student	123	2131	0752503016
86	Blaga	Matei	matei.blaga@student	123	2141	0760183397
87	Sigartau	Diana	diana.sigartau@student	123	2141	0789251797
88	Furtos	Alexandru	alexandru.furtos@student	123	2141	0770684330
33	Mikle	Carla Ariana	carlaariana.mikle@student	123	2121	0731306909
89	Furtos	Raluca	raluca.furtos@student	123	2141	0746335002
90	Munteanu	Iulian	iulian.munteanu@student	123	2141	0777578389
91	Marcu	Adriana	adriana.marcu@student	123	2141	0729497701
92	Radu	Marian	marian.radu@student	123	2141	0773335261
93	Marc	Adela	adela.marc@student	123	2141	0751132221
94	Muresan	Carina	carina.muresan@student	123	2141	0786860314
95	Sandru	Stefania	stefania.sandru@student	123	2141	0786900078
96	Moga	David	david.moga@student	123	2141	0769535068
97	Vultur	Gratiana	gratiana.vultur@student	123	2141	0796786705
98	Miholca	Adrian	adrian.miholca@student	123	2141	0731957777
99	Tantau	Luciana	luciana.tantau@student	123	2141	0794935712
100	Ratiu	David	david.ratiu@student	123	2141	0771784704
102	Iacob	Stefania	stefania.iacob@student	123	2141	0786705025
103	Lupse	Radu	radu.lupse@student	123	2141	0768556450
104	Radu	Valentin	valentin.radu@student	123	2141	0729255536
105	Sumalan	Emanuela	emanuela.sumalan@student	123	2141	0773019552
106	Gal	Matei	matei.gal@student	123	2141	0788881790
107	Popescu	David	david.popescu@student	123	2141	0723561691
108	Grec	Iarina	iarina.grec@student	123	2141	0787949288
109	Ghemes	Andrei	andrei.ghemes@student	123	2141	0715059090
110	Rusu	Camelia	camelia.rusu@student	123	2141	0716859849
111	Pop	Darian	darian.pop@student	123	2141	0736313369
112	Popovici	Gabriel	gabriel.popovici@student	123	2141	0771028388
113	Bontos	Valentin	valentin.bontos@student	123	2112	0721992256
114	Furtos	Stefan	stefan.furtos@student	123	2112	0797236600
115	Negoiescu	Alexandru	alexandru.negoiescu@student	123	2112	0744831623
116	Lenghel	Cristian	cristian.lenghel@student	123	2112	0747096596
117	Isaiu	Ionut	ionut.isaiu@student	123	2112	0780114014
118	Sigartau	Lucas	lucas.sigartau@student	123	2112	0717439137
120	Cornici	Ciprian	ciprian.cornici@student	123	2112	0726277755
121	Ilies	Ciprian	ciprian.ilies@student	123	2112	0736971606
122	Bontos	Gratiana	gratiana.bontos@student	123	2112	0786512926
123	Ionescu	Andreea	andreea.ionescu@student	123	2112	0760602079
124	Soponar	Camelia	camelia.soponar@student	123	2112	0777181389
125	Popovici	Mircea	mircea.popovici@student	123	2112	0772532752
126	Lazar	Stefan	stefan.lazar@student	123	2112	0783494712
127	Sumalan	Stefania	stefania.sumalan@student	123	2112	0768251967
128	Bontos	Valentina	valentina.bontos@student	123	2112	0750424088
129	Daboc	Laura	laura.daboc@student	123	2112	0725144898
130	Isaiu	Emma	emma.isaiu@student	123	2112	0734752284
131	Corbean	Raluca	raluca.corbean@student	123	2112	0765770153
132	Balea	Lucian	lucian.balea@student	123	2112	0734868582
133	Marginas	Marin	marin.marginas@student	123	2112	0758508633
134	Dumitru	Adela	adela.dumitru@student	123	2112	0731326526
135	Contra	Denisa	denisa.contra843@student	123	2112	0792787214
136	Stan	Claudia	claudia.stan@student	123	2112	0756666145
137	Ciutan	Gabriel	gabriel.ciutan@student	123	2112	0756980090
138	Conea	Alexia	alexia.conea@student	123	2112	0769798741
139	Ionescu	Diana	diana.ionescu@student	123	2112	0769405439
140	Ratiu	Denisa	denisa.ratiu@student	123	2122	0738179684
142	Corbean	Patricia	patricia.corbean@student	123	2122	0721837822
143	Sandu	Radu	radu.sandu@student	123	2122	0784038193
144	Iliasa	Mihaela	mihaela.iliasa@student	123	2122	0714232271
145	Ratiu	Alexia	alexia.ratiu@student	123	2122	0743802718
146	Balaj	Carina	carina.balaj@student	123	2122	0799120075
147	Chiorean	Stefan	stefan.chiorean@student	123	2122	0758733926
148	Ratiu	Darian	darian.ratiu@student	123	2122	0724266093
149	Apostu	Gratian	gratian.apostu@student	123	2122	0719101676
150	Boesan	Cristina	cristina.boesan@student	123	2122	0742993645
151	Sabou	Cristina	cristina.sabou@student	123	2122	0752330316
152	Talpos	Camelia	camelia.talpos@student	123	2122	0774167763
153	Ghemes	Octavian	octavian.ghemes@student	123	2122	0725787122
154	Cucura	Adela	adela.cucura@student	123	2122	0775009447
155	Craciun	Radu	radu.craciun@student	123	2122	0726817689
156	Santeiu	Nicolas	nicolas.santeiu@student	123	2122	0798212088
158	Negru	Nicoleta	nicoleta.negru@student	123	2122	0769767706
159	Iancu	Elena	elena.iancu@student	123	2122	0793840977
160	Lazar	Gratiana	gratiana.lazar@student	123	2122	0775173993
161	Ghemes	Daria	daria.ghemes@student	123	2122	0745355396
162	Cucura	Mihai	mihai.cucura@student	123	2122	0787220309
163	Apostu	Adela	adela.apostu@student	123	2122	0730989922
164	Tantau	Radu	radu.tantau@student	123	2122	0794978830
165	Udvari	Mircea	mircea.udvari@student	123	2122	0755268342
166	Stoica	Marian	marian.stoica@student	123	2122	0754805885
167	Mikle	Thomas	thomas.mikle@student	123	2122	0717605010
168	Sandu	Mircea	mircea.sandu@student	123	2122	0768294179
169	Sasarman	Ioana	ioana.sasarman@student	123	2132	0733403958
170	Achim	Claudia	claudia.achim@student	123	2132	0751513559
171	Stroia	Iulia	iulia.stroia@student	123	2132	0728085422
172	Bontos	Alin	alin.bontos@student	123	2132	0753635895
173	Dumitru	Octavian	octavian.dumitru@student	123	2132	0722883459
174	Galis	Vlad	vlad.galis@student	123	2132	0712078636
175	Stan	Alina	alina.stan@student	123	2132	0734681241
176	Mikle	Adela	adela.mikle@student	123	2132	0726554341
101	Rus	Carla Ariana	carlaariana.rus@student	123	2141	0768314594
177	Dumitru	Carla Ariana	carlaariana.dumitru@student	123	2132	0734606803
178	Popovici	Cristian	cristian.popovici@student	123	2132	0775592098
179	Cucura	Andreea	andreea.cucura@student	123	2132	0738063753
180	Contra	David	david.contra@student	123	2132	0753417246
181	Marc	Emanuela	emanuela.marc@student	123	2132	0715327103
183	Ari	Adrian	adrian.ari@student	123	2132	0748054415
184	Dobra	Miruna	miruna.dobra@student	123	2132	0750146708
185	Chiorean	Emanuel	emanuel.chiorean@student	123	2132	0762772053
186	Cioara	Emanuela	emanuela.cioara@student	123	2132	0715513888
187	Moga	Catinca	catinca.moga@student	123	2132	0765988777
188	Lauriuc	Florentina	florentina.lauriuc@student	123	2132	0793021713
189	Boangher	Stefan	stefan.boangher@student	123	2132	0730090265
190	Apostu	Lucas	lucas.apostu@student	123	2132	0746685601
191	Sandu	Lucas	lucas.sandu@student	123	2132	0739695055
192	Dumitru	Iulian	iulian.dumitru@student	123	2132	0792026069
193	Mikle	Luciana	luciana.mikle@student	123	2132	0714256023
194	Ghysa	Marin	marin.ghysa@student	123	2132	0789391451
195	Furtos	Ciprian	ciprian.furtos@student	123	2132	0770873947
196	Radu	Marin	marin.radu@student	123	2132	0757388964
197	Lazar	Emma	emma.lazar@student	123	2132	0771401224
198	Cretu	Tudor	tudor.cretu@student	123	2142	0719533395
199	Stroia	Matei	matei.stroia@student	123	2142	0766004093
200	Galis	Rodica	rodica.galis@student	123	2142	0792543732
201	Cotitu	Catinca	catinca.cotitu@student	123	2142	0791737027
202	Muresan	Emanuela	emanuela.muresan@student	123	2142	0769674962
203	Sabou	Alin	alin.sabou@student	123	2142	0745961550
204	Ortan	Ioana	ioana.ortan@student	123	2142	0749825048
205	Susa	Thomas	thomas.susa@student	123	2142	0741892535
206	Popovici	Vlad	vlad.popovici@student	123	2142	0734598599
207	Bucur	Stefan	stefan.bucur@student	123	2142	0789567131
208	Chihaia	Lucian	lucian.chihaia@student	123	2142	0763061528
209	Stoian	Iarina	iarina.stoian@student	123	2142	0711509016
210	Conea	Nicolas	nicolas.conea@student	123	2142	0787226061
211	Moiceanu	Ionela	ionela.moiceanu@student	123	2142	0791158694
212	Cotitu	Oana	oana.cotitu@student	123	2142	0797353744
213	Stan	Vlad	vlad.stan@student	123	2142	0777095972
214	Maidan	Nicolas	nicolas.maidan@student	123	2142	0765806663
215	Negru	Oana	oana.negru@student	123	2142	0740958997
216	Bejan	Marin	marin.bejan@student	123	2142	0734150945
217	Cornici	Vlad	vlad.cornici@student	123	2142	0788819601
218	Bidelita	Cristian	cristian.bidelita@student	123	2142	0773267366
219	Belciu	Andrei	andrei.belciu@student	123	2142	0716264492
220	Dobre	Teodor	teodor.dobre@student	123	2142	0792820392
221	Barbu	Oana	oana.barbu@student	123	2142	0722905208
222	Cretu	Claudiu	claudiu.cretu@student	123	2142	0736722460
223	Popa	Alin	alin.popa@student	123	2142	0728404305
224	Valean	Rodica	rodica.valean@student	123	2142	0773292632
225	Mozacu	Diana	diana.mozacu@student	123	2142	0794668436
227	Marc	Ionut	ionut.marc@student	123	2113	0747119423
228	Gheiceanu	Stefana	stefana.gheiceanu@student	123	2113	0757184889
229	Comsa	Gabriel	gabriel.comsa@student	123	2113	0795148927
230	Blidar	Patric	patric.blidar@student	123	2113	0799579907
231	Corbean	Mihaela	mihaela.corbean@student	123	2113	0745003053
232	Bucur	Alin	alin.bucur@student	123	2113	0753354384
233	Popovici	Carina	carina.popovici@student	123	2113	0756304405
234	Miholca	Serban	serban.miholca@student	123	2113	0747791657
235	Contra	Denis	denis.contra@student	123	2113	0744520133
236	Cotitu	Emma	emma.cotitu@student	123	2113	0796303131
237	Dindiligan	Emanuel	emanuel.dindiligan@student	123	2113	0793974130
238	Sandu	Diana	diana.sandu@student	123	2113	0775064020
239	Soponar	Denis	denis.soponar@student	123	2113	0711646822
240	Haiduc	Maia	maia.haiduc@student	123	2113	0752057018
241	Dobre	Radu	radu.dobre@student	123	2113	0787685582
242	Fanatan	Marin	marin.fanatan@student	123	2113	0767935222
243	Iacob	Daria	daria.iacob@student	123	2113	0712445660
245	Bolojan	Mihai	mihai.bolojan@student	123	2113	0740185421
246	Susa	Laura	laura.susa@student	123	2113	0734453177
247	Sigartau	Ioana	ioana.sigartau@student	123	2113	0797268555
248	Blaga	Andrei	andrei.blaga@student	123	2113	0735646175
250	Cotitu	Florentina	florentina.cotitu@student	123	2113	0783842399
251	Gal	Gratiana	gratiana.gal@student	123	2113	0719623419
252	Bolovan	Alin	alin.bolovan@student	123	2113	0735265343
253	Santeiu	Ioana	ioana.santeiu@student	123	2113	0738974642
255	Maidan	Stefan	stefan.maidan@student	123	2123	0789154348
256	Ghemes	Mihai	mihai.ghemes@student	123	2123	0725982786
257	Gal	Daria	daria.gal@student	123	2123	0742079240
258	Valean	Ionela	ionela.valean@student	123	2123	0759712668
259	Belciu	Bianca	bianca.belciu@student	123	2123	0757128432
260	Constantin	Stefania	stefania.constantin@student	123	2123	0716689975
261	Boesan	Claudiu	claudiu.boesan@student	123	2123	0773322086
262	Demian	Patricia	patricia.demian@student	123	2123	0756401687
263	Balaj	Valentin	valentin.balaj@student	123	2123	0763657164
264	Stroia	Octavian	octavian.stroia@student	123	2123	0736675487
265	Grec	Oana	oana.grec@student	123	2123	0721453524
266	Ciubotaru	Alin	alin.ciubotaru@student	123	2123	0790615339
182	Sasarman	Carla Ariana	carlaariana.sasarman@student	123	2132	0739038223
254	Achim	Darius	darius.achim@student	123	2122	0777303713
267	Dindiligan	Mircea	mircea.dindiligan@student	123	2123	0733827797
268	Mikle	Bogdan	bogdan.mikle@student	123	2123	0738739894
269	Pocol	Marin	marin.pocol@student	123	2123	0784964218
270	Mikle	Marin	marin.mikle@student	123	2123	0756611929
271	Antohi	Cristina	cristina.antohi@student	123	2123	0793448202
272	Balea	Nicolas	nicolas.balea@student	123	2123	0740782047
273	Cretu	Maria	maria.cretu@student	123	2123	0727370690
274	Budnar	Iulia	iulia.budnar@student	123	2123	0773385139
275	Comsa	Daria	daria.comsa@student	123	2123	0738094807
276	Constantin	Laura	laura.constantin@student	123	2123	0719227443
277	Valean	Darius	darius.valean@student	123	2123	0727048085
278	Dobra	Nicoleta	nicoleta.dobra@student	123	2123	0764831805
279	Bidelita	Luciana	luciana.bidelita@student	123	2123	0763403487
280	Sasaran	Adina	adina.sasaran@student	123	2123	0732871849
282	Dobra	Adina	adina.dobra@student	123	2123	0798709777
283	Bidelita	Gabriela	gabriela.bidelita@student	123	2123	0777090963
284	Gherasim	Iulian	iulian.gherasim@student	123	2133	0795749737
285	Barbu	Maia	maia.barbu@student	123	2133	0771997976
286	Ilies	David	david.ilies@student	123	2133	0722269180
287	Mozacu	Alexia	alexia.mozacu@student	123	2133	0790808303
288	Blidar	Darian	darian.blidar@student	123	2133	0758846112
289	Eminescu	Matei	matei.eminescu@student	123	2133	0767950124
290	Antohi	Maia	maia.antohi@student	123	2133	0785871214
291	Dobre	Marian	marian.dobre@student	123	2133	0730298111
292	Cheta	Larisa	larisa.cheta@student	123	2133	0788978900
293	Stoica	David	david.stoica@student	123	2133	0779109292
294	Chiorean	Carina	carina.chiorean@student	123	2133	0733557022
295	Dolca	Teodor	teodor.dolca@student	123	2133	0748564761
296	Lupse	Marius	marius.lupse@student	123	2133	0786053269
297	Moiceanu	Teodor	teodor.moiceanu@student	123	2133	0788150339
298	Fanatan	Darian	darian.fanatan@student	123	2133	0713431482
299	Comsa	Andreea	andreea.comsa@student	123	2133	0742586152
300	Sigartau	Alexia	alexia.sigartau@student	123	2133	0740203806
301	Antoniade	Alin	alin.antoniade@student	123	2133	0768417287
302	Sigartau	Cristina	cristina.sigartau@student	123	2133	0780338237
303	Sandu	Camelia	camelia.sandu@student	123	2133	0749302139
304	Bolovan	Gabriel	gabriel.bolovan@student	123	2133	0716430966
305	Dobra	Nicolas	nicolas.dobra@student	123	2133	0728042278
306	Antohi	Adina	adina.antohi@student	123	2133	0788521508
307	Dolca	Daria	daria.dolca@student	123	2133	0738657504
308	Antohi	Mihaela	mihaela.antohi@student	123	2133	0767588435
310	Udvari	Gratiana	gratiana.udvari@student	123	2143	0755236127
311	Iliasa	Valentin	valentin.iliasa@student	123	2143	0783371096
312	Dobre	Adriana	adriana.dobre@student	123	2143	0793847493
313	Negru	Mihaela	mihaela.negru@student	123	2143	0766353699
314	Dobre	Catinca	catinca.dobre@student	123	2143	0754887299
315	Bolojan	Alina	alina.bolojan@student	123	2143	0777268091
316	Daboc	Vlad	vlad.daboc@student	123	2143	0755911366
317	Sumalan	Camelia	camelia.sumalan@student	123	2143	0754542618
318	Belciu	Radu	radu.belciu@student	123	2143	0788207164
319	Miholca	Marian	marian.miholca@student	123	2143	0724094152
320	Bidelita	Ioana	ioana.bidelita@student	123	2143	0731468320
321	Rusu	Patric	patric.rusu@student	123	2143	0759976733
322	Marc	Marius	marius.marc@student	123	2143	0768966825
323	Stoian	Darius	darius.stoian@student	123	2143	0752345776
324	Bucur	Elena	elena.bucur@student	123	2143	0740086404
325	Popovici	Stefana	stefana.popovici@student	123	2143	0728855237
326	Stan	Catalin	catalin.stan@student	123	2143	0747852209
327	Mozacu	Mihai	mihai.mozacu@student	123	2143	0747865436
328	Constantin	David	david.constantin@student	123	2143	0745687118
329	Ari	Andrei	andrei.ari@student	123	2143	0721339504
330	Nistor	Oana	oana.nistor@student	123	2143	0722892579
331	Corbean	Calin	calin.corbean@student	123	2143	0745681231
332	Harghel	Darian	darian.harghel@student	123	2143	0740941242
333	Harghel	Mihai	mihai.harghel@student	123	2143	0773334400
334	Marginas	Adina	adina.marginas@student	123	2143	0714341050
335	Marginas	Alin	alin.marginas@student	123	2143	0791449656
336	Iliasa	Emanuela	emanuela.iliasa@student	123	2143	0765404282
337	Talpos	Thomas	thomas.talpos@student	123	2211	0720360684
339	Bontos	Stefan	stefan.bontos@student	123	2211	0782986540
340	Ratiu	Adrian	adrian.ratiu@student	123	2211	0726715142
341	Haiduc	Ioana	ioana.haiduc@student	123	2211	0719708455
342	Galis	Teodora	teodora.galis@student	123	2211	0711789170
343	Bolovan	Nicolas	nicolas.bolovan@student	123	2211	0749022209
344	Gheorghe	Carina	carina.gheorghe@student	123	2211	0730955232
345	Alb	Ionela	ionela.alb@student	123	2211	0727106952
346	Oltean	Cristina	cristina.oltean@student	123	2211	0724245129
347	Iliasa	Gabriela	gabriela.iliasa@student	123	2211	0778149156
348	Mozacu	Iulian	iulian.mozacu@student	123	2211	0784169027
349	Marginas	Thomas	thomas.marginas@student	123	2211	0742688457
350	Dolca	Bogdan	bogdan.dolca@student	123	2211	0722047445
351	Marinescu	Thomas	thomas.marinescu@student	123	2211	0731304918
352	Timis	Mihaela	mihaela.timis@student	123	2211	0725963744
353	Muresan	Luciana	luciana.muresan@student	123	2211	0754305811
354	Cucura	Alin	alin.cucura@student	123	2211	0797956506
356	Harghel	Ciprian	ciprian.harghel@student	123	2211	0738560896
357	Budnar	Marin	marin.budnar@student	123	2211	0764193652
358	Bejan	Iulia	iulia.bejan@student	123	2211	0730275323
359	Sasaran	Alina	alina.sasaran@student	123	2211	0786284892
360	Miholca	Gratian	gratian.miholca@student	123	2211	0757872838
361	Belciu	Patricia	patricia.belciu@student	123	2211	0783628872
362	Gal	Radu	radu.gal@student	123	2211	0787269546
363	Comsa	Ionut	ionut.comsa@student	123	2211	0740980013
364	Cheta	Valentina	valentina.cheta@student	123	2211	0773935503
367	Ortan	Alina	alina.ortan@student	123	2221	0791133171
368	Bontos	Matei	matei.bontos@student	123	2221	0761636409
369	Contra	Ionela	ionela.contra@student	123	2221	0745139049
370	Cioara	Claudiu	claudiu.cioara@student	123	2221	0714019510
371	Iacob	Ionela	ionela.iacob@student	123	2221	0741050091
372	Daboc	Diana	diana.daboc@student	123	2221	0727898652
373	Boangher	Ciprian	ciprian.boangher@student	123	2221	0741198732
374	Cheta	Calin	calin.cheta@student	123	2221	0727351293
375	Pocol	Lucian	lucian.pocol@student	123	2221	0789451589
376	Marcu	Valentina	valentina.marcu@student	123	2221	0735777455
377	Lazar	Andrei	andrei.lazar@student	123	2221	0718802023
378	Munteanu	Ciprian	ciprian.munteanu@student	123	2221	0765678522
379	Craciun	Mircea	mircea.craciun@student	123	2221	0724143187
380	Contra	Elena	elena.contra@student	123	2221	0724407707
381	Budnar	Catinca	catinca.budnar@student	123	2221	0798084371
382	Cretu	Gabriela	gabriela.cretu@student	123	2221	0717770372
383	Timis	Denis	denis.timis@student	123	2221	0725740590
384	Corbean	Maria	maria.corbean@student	123	2221	0783591046
385	Popa	Bogdan	bogdan.popa@student	123	2221	0799345163
386	Cucura	Diana	diana.cucura@student	123	2221	0726548182
387	Sandu	Larisa	larisa.sandu@student	123	2221	0775321780
388	Stoian	Calin	calin.stoian@student	123	2221	0791297606
389	Chiuzbaian	Marcela	marcela.chiuzbaian@student	123	2221	0768507162
390	Cornici	Gabriela	gabriela.cornici@student	123	2221	0766790943
391	Galis	Alina	alina.galis@student	123	2221	0754652021
392	Lupse	Camelia	camelia.lupse@student	123	2221	0736433448
393	Udvari	Darian	darian.udvari@student	123	2221	0733528125
395	Isaiu	Rodica	rodica.isaiu@student	123	2231	0751473885
396	Belciu	Ioana	ioana.belciu@student	123	2231	0789800373
397	Alb	Nicolas	nicolas.alb@student	123	2231	0756985017
398	Balaj	Andreea	andreea.balaj@student	123	2231	0786720418
399	Stan	Stefan	stefan.stan@student	123	2231	0754228394
400	Adam	Vlad	vlad.adam@student	123	2231	0723097887
402	Talpos	Traian	traian.talpos@student	123	2231	0767490705
403	Miholca	Gratiana	gratiana.miholca@student	123	2231	0771134678
404	Ionescu	Patricia	patricia.ionescu@student	123	2231	0779854340
405	Valean	Alexandra	alexandra.valean@student	123	2231	0745305406
406	Demian	Larisa	larisa.demian@student	123	2231	0734933401
407	Dindiligan	Gabriel	gabriel.dindiligan@student	123	2231	0768252934
408	Timis	Daria	daria.timis@student	123	2231	0747361791
409	Sandu	Alexandra	alexandra.sandu@student	123	2231	0795918134
410	Daboc	Serban	serban.daboc@student	123	2231	0761796192
411	Blaga	Denisa	denisa.blaga@student	123	2231	0728578017
412	Harghel	Mircea	mircea.harghel@student	123	2231	0725518545
413	Popa	Marcela	marcela.popa@student	123	2231	0725824388
414	Udvari	Diana	diana.udvari@student	123	2231	0792467894
415	Socaci	Nicoleta	nicoleta.socaci@student	123	2231	0723695889
416	Sandru	Stefana	stefana.sandru@student	123	2231	0719210263
417	Creanga	Maria	maria.creanga@student	123	2231	0775562535
419	Gal	Nicolas	nicolas.gal@student	123	2231	0769492901
420	Cioara	Patricia	patricia.cioara@student	123	2231	0759511118
421	Maidan	Marcela	marcela.maidan@student	123	2231	0716242186
422	Boesan	Alessia	alessia.boesan@student	123	2241	0795708147
423	Talpos	Claudia	claudia.talpos@student	123	2241	0744869713
424	Chiuzbaian	Lucian	lucian.chiuzbaian@student	123	2241	0727336362
425	Demian	Nicoleta	nicoleta.demian@student	123	2241	0725497145
426	Blaga	Miruna	miruna.blaga@student	123	2241	0729934103
427	Bolovan	Ciprian	ciprian.bolovan@student	123	2241	0749149154
428	Popa	Ioana	ioana.popa@student	123	2241	0776427615
429	Ghysa	Thomas	thomas.ghysa@student	123	2241	0758491341
430	Stoian	Alessia	alessia.stoian@student	123	2241	0760049580
431	Popovici	Emanuel	emanuel.popovici@student	123	2241	0723119367
432	Haiduc	Catinca	catinca.haiduc@student	123	2241	0759640664
434	Alb	Stefan	stefan.alb@student	123	2241	0768059113
436	Grec	Maria	maria.grec@student	123	2241	0762896592
437	Cucura	Alexandru	alexandru.cucura@student	123	2241	0729625535
438	Cucura	Catalin	catalin.cucura@student	123	2241	0730449086
439	Antoniade	Marius	marius.antoniade@student	123	2241	0732265684
440	Apostu	Maia	maia.apostu@student	123	2241	0794890982
441	Hambasan	Gabriela	gabriela.hambasan@student	123	2241	0732862774
442	Gal	Ioana	ioana.gal@student	123	2241	0751088366
443	Vultur	Alexandra	alexandra.vultur@student	123	2241	0747700288
433	Balea	Carla Ariana	carlaariana.balea@student	123	2241	0759224190
435	Ciubotaru	Carla Ariana	carlaariana.ciubotaru@student	123	2241	0756714014
444	Barbu	Teodora	teodora.barbu@student	123	2241	0758000411
445	Udvari	Darius	darius.udvari@student	123	2241	0795938468
446	Ari	Gratian	gratian.ari@student	123	2241	0744522898
447	Negru	Octavian	octavian.negru@student	123	2241	0794032548
448	Sasaran	Thomas	thomas.sasaran@student	123	2241	0748600971
449	Ari	Cristina	cristina.ari@student	123	2241	0761183018
450	Dumitru	Claudia	claudia.dumitru@student	123	2212	0717529981
451	Antoniade	Denis	denis.antoniade@student	123	2212	0713897212
452	Bidelita	Larisa	larisa.bidelita@student	123	2212	0730748633
453	Soponar	Gabriela	gabriela.soponar@student	123	2212	0753808451
455	Dobra	Teodor	teodor.dobra@student	123	2212	0740540110
456	Cotitu	Ioana	ioana.cotitu@student	123	2212	0714754698
457	Gheorghe	Denisa	denisa.gheorghe@student	123	2212	0730289612
458	Harghel	Alexandra	alexandra.harghel@student	123	2212	0712842768
459	Bucur	Laura	laura.bucur@student	123	2212	0717687820
460	Cioara	Ionut	ionut.cioara@student	123	2212	0753856640
461	Chiuzbaian	Ionela	ionela.chiuzbaian@student	123	2212	0788229270
462	Munteanu	Carina	carina.munteanu@student	123	2212	0747636955
463	Rus	Raluca	raluca.rus@student	123	2212	0779996359
464	Pocol	Ciprian	ciprian.pocol@student	123	2212	0762793705
465	Susa	Vlad	vlad.susa@student	123	2212	0757971221
466	Boesan	Alexandru	alexandru.boesan@student	123	2212	0724094839
467	Munteanu	Marcela	marcela.munteanu@student	123	2212	0739420539
468	Lauriuc	Tudor	tudor.lauriuc@student	123	2212	0782532217
469	Indre	Catalin	catalin.indre@student	123	2212	0731378071
470	Cioara	Claudia	claudia.cioara@student	123	2212	0758429756
471	Lauriuc	Mircea	mircea.lauriuc@student	123	2212	0752892906
472	Dolca	Marius	marius.dolca@student	123	2212	0747878458
473	Coman	Cristina	cristina.coman@student	123	2212	0716847863
474	Isaiu	Marin	marin.isaiu@student	123	2212	0725895811
475	Apostu	Calin	calin.apostu@student	123	2212	0778710879
476	Basa	Catalin	catalin.basa@student	123	2212	0777477999
477	Muresan	Gabriela	gabriela.muresan@student	123	2222	0746636170
479	Lauriuc	Vlad	vlad.lauriuc@student	123	2222	0779481213
480	Ratiu	Mihai	mihai.ratiu@student	123	2222	0768257821
481	Ciutan	Ciprian	ciprian.ciutan@student	123	2222	0724301313
482	Bolojan	Gabriel	gabriel.bolojan@student	123	2222	0787474586
483	Gheorghe	Alexandra	alexandra.gheorghe@student	123	2222	0721122190
484	Constantin	Adriana	adriana.constantin@student	123	2222	0782210678
485	Lazar	Raluca	raluca.lazar@student	123	2222	0782953198
486	Cucura	Calin	calin.cucura@student	123	2222	0759838848
487	Ilies	Traian	traian.ilies@student	123	2222	0733939639
488	Socaci	Bianca	bianca.socaci@student	123	2222	0738712685
489	Moga	Emma	emma.moga@student	123	2222	0769263403
490	Radu	Alexia	alexia.radu@student	123	2222	0779078840
491	Cotitu	Teodor	teodor.cotitu@student	123	2222	0735094408
492	Marinescu	Catalin	catalin.marinescu@student	123	2222	0772754291
494	Susa	Teodora	teodora.susa@student	123	2222	0773043365
495	Achim	Florentina	florentina.achim@student	123	2222	0791796259
496	Marinescu	Elena	elena.marinescu@student	123	2222	0755876163
497	Udvari	Florentina	florentina.udvari@student	123	2222	0744493522
498	Talpos	Serban	serban.talpos@student	123	2222	0751750847
499	Muresan	Lucas	lucas.muresan@student	123	2222	0762302750
500	Sigartau	Bianca	bianca.sigartau@student	123	2222	0726473951
501	Ghysa	Bogdan	bogdan.ghysa@student	123	2222	0765721604
502	Berindan	Patric	patric.berindan@student	123	2222	0775839369
503	Lazar	Stefania	stefania.lazar@student	123	2222	0785857775
504	Popa	Ionela	ionela.popa@student	123	2222	0786138658
505	Moga	Denisa	denisa.moga@student	123	2232	0780446934
506	Corbean	Emma	emma.corbean@student	123	2232	0754484693
507	Dumitru	Iarina	iarina.dumitru@student	123	2232	0787845598
508	Basa	Laura	laura.basa@student	123	2232	0715357848
509	Tantau	Denisa	denisa.tantau@student	123	2232	0726819053
511	Budnar	Nicoleta	nicoleta.budnar@student	123	2232	0715166490
512	Tantau	Ioana	ioana.tantau@student	123	2232	0773892980
513	Demian	Denisa	denisa.demian@student	123	2232	0726045491
514	Groza	Vlad	vlad.groza@student	123	2232	0719139358
515	Sabou	Patricia	patricia.sabou@student	123	2232	0742167598
516	Sasaran	Claudiu	claudiu.sasaran@student	123	2232	0712808543
519	Galis	Bogdan	bogdan.galis@student	123	2232	0732146870
520	Bidelita	Calin	calin.bidelita@student	123	2232	0785671469
521	Gheiceanu	Gabriel	gabriel.gheiceanu@student	123	2232	0750239028
522	Miholca	Alina	alina.miholca@student	123	2232	0765726607
523	Barbu	Mircea	mircea.barbu@student	123	2232	0755561257
524	Marc	Gratiana	gratiana.marc@student	123	2232	0711401387
525	Haiduc	Radu	radu.haiduc@student	123	2232	0744125305
526	Antohi	Valentina	valentina.antohi@student	123	2232	0747630605
527	Ghemes	Carina	carina.ghemes@student	123	2232	0773940540
528	Furtos	Luciana	luciana.furtos@student	123	2232	0743628504
529	Bolovan	Marius	marius.bolovan@student	123	2232	0767302459
530	Ciutan	Nicoleta	nicoleta.ciutan@student	123	2232	0752912771
531	Achim	Thomas	thomas.achim@student	1234567890	2242	0798898573
532	Creanga	Teodora	teodora.creanga@student	123	2242	0790485442
533	Chiorean	Nicolas	nicolas.chiorean@student	123	2242	0775719845
534	Gherasim	Catinca	catinca.gherasim@student	123	2242	0715557491
536	Chihaia	Teodor	teodor.chihaia@student	123	2242	0735761541
537	Barbu	Iulia	iulia.barbu@student	123	2242	0780786434
538	Bolojan	Emma	emma.bolojan@student	123	2242	0797259777
539	Chiuzbaian	Stefania	stefania.chiuzbaian@student	123	2242	0728860209
540	Cornici	Iulia	iulia.cornici@student	123	2242	0736812048
541	Indre	Emma	emma.indre@student	123	2242	0718257404
542	Ghemes	Rodica	rodica.ghemes@student	123	2242	0736206046
543	Chihaia	Ioana	ioana.chihaia@student	123	2242	0744705931
544	Gherasim	Ionut	ionut.gherasim@student	123	2242	0722091577
546	Bolovan	Oana	oana.bolovan@student	123	2242	0743048363
547	Mikle	Florentina	florentina.mikle@student	123	2242	0714009230
548	Santeiu	Ionela	ionela.santeiu@student	123	2242	0780689079
549	Marinescu	Patric	patric.marinescu@student	123	2242	0744063454
550	Iacob	Valentin	valentin.iacob@student	123	2242	0715524430
551	Susa	Emanuel	emanuel.susa@student	123	2242	0715331358
552	Pantis	Marin	marin.pantis@student	123	2242	0781722933
553	Sasaran	Mircea	mircea.sasaran@student	123	2242	0726649369
554	Craciun	Bogdan	bogdan.craciun@student	123	2242	0737778782
555	Harghel	Raluca	raluca.harghel@student	123	2242	0760729533
556	Sumalan	Tudor	tudor.sumalan@student	123	2242	0717923792
557	Miholca	Camelia	camelia.miholca@student	123	2242	0781992447
226	Achim	Alexandru	alexandru.achim@student	1234	2112	0727589513
244	Marcu	Carla Ariana	carlaariana.marcu@student	123	2113	0798803591
249	Apostu	Carla Ariana	carlaariana.apostu@student	123	2113	0779211437
558	Cornici	Andreea	andreea.cornici@student	suntproasta	2222	0744144944
560	Belciu	Carla	carla.belciu@student	suntproasta	2222	0744144944
562	Pantis	Patricia	patricia.pantis@student	123	2222	0744144944
563	Cotitu	Daria	daria.cotitu@student	123	2222	0744144944
566	Cotitu	Daria Luana	darialuana.cotitu@student	123	2221	0744144944
567	Cornici	Andreea	andreea.cornici1@student	123	2221	0744144944
545	Achim	Laura	laura.achim@student	1234	2241	0781650607
\.


--
-- TOC entry 4965 (class 0 OID 0)
-- Dependencies: 226
-- Name: admini_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.admini_id_seq', 5, true);


--
-- TOC entry 4966 (class 0 OID 0)
-- Dependencies: 224
-- Name: catalog_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.catalog_id_seq', 64, true);


--
-- TOC entry 4967 (class 0 OID 0)
-- Dependencies: 220
-- Name: materie_id_materie_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.materie_id_materie_seq', 40, true);


--
-- TOC entry 4968 (class 0 OID 0)
-- Dependencies: 218
-- Name: profesori_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.profesori_id_seq', 6, true);


--
-- TOC entry 4969 (class 0 OID 0)
-- Dependencies: 222
-- Name: studenti_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.studenti_id_seq', 568, true);


--
-- TOC entry 4790 (class 2606 OID 17209)
-- Name: admini admini_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.admini
    ADD CONSTRAINT admini_email_key UNIQUE (email);


--
-- TOC entry 4792 (class 2606 OID 17207)
-- Name: admini admini_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.admini
    ADD CONSTRAINT admini_pkey PRIMARY KEY (id);


--
-- TOC entry 4788 (class 2606 OID 17010)
-- Name: catalog catalog_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.catalog
    ADD CONSTRAINT catalog_pkey PRIMARY KEY (id);


--
-- TOC entry 4776 (class 2606 OID 16823)
-- Name: grupe grupe_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.grupe
    ADD CONSTRAINT grupe_pkey PRIMARY KEY (id);


--
-- TOC entry 4782 (class 2606 OID 16854)
-- Name: materie materie_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.materie
    ADD CONSTRAINT materie_pkey PRIMARY KEY (id_materie);


--
-- TOC entry 4778 (class 2606 OID 16832)
-- Name: profesori profesori_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.profesori
    ADD CONSTRAINT profesori_email_key UNIQUE (email);


--
-- TOC entry 4780 (class 2606 OID 16830)
-- Name: profesori profesori_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.profesori
    ADD CONSTRAINT profesori_pkey PRIMARY KEY (id);


--
-- TOC entry 4784 (class 2606 OID 16997)
-- Name: studenti studenti_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.studenti
    ADD CONSTRAINT studenti_email_key UNIQUE (email);


--
-- TOC entry 4786 (class 2606 OID 16995)
-- Name: studenti studenti_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.studenti
    ADD CONSTRAINT studenti_pkey PRIMARY KEY (id);


--
-- TOC entry 4798 (class 2620 OID 17133)
-- Name: catalog trigger_verificare_notare; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_verificare_notare BEFORE INSERT ON public.catalog FOR EACH ROW EXECUTE FUNCTION public.verifica_permisiune_notare();


--
-- TOC entry 4795 (class 2606 OID 17178)
-- Name: catalog catalog_id_materie_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.catalog
    ADD CONSTRAINT catalog_id_materie_fkey FOREIGN KEY (id_materie) REFERENCES public.materie(id_materie) ON DELETE CASCADE;


--
-- TOC entry 4796 (class 2606 OID 17016)
-- Name: catalog catalog_id_profesor_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.catalog
    ADD CONSTRAINT catalog_id_profesor_fkey FOREIGN KEY (id_profesor) REFERENCES public.profesori(id) ON DELETE CASCADE;


--
-- TOC entry 4797 (class 2606 OID 17173)
-- Name: catalog catalog_id_student_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.catalog
    ADD CONSTRAINT catalog_id_student_fkey FOREIGN KEY (id_student) REFERENCES public.studenti(id) ON DELETE CASCADE;


--
-- TOC entry 4793 (class 2606 OID 16855)
-- Name: materie materie_id_profesor_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.materie
    ADD CONSTRAINT materie_id_profesor_fkey FOREIGN KEY (id_profesor) REFERENCES public.profesori(id) ON DELETE CASCADE;


--
-- TOC entry 4794 (class 2606 OID 16998)
-- Name: studenti studenti_id_grupa_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.studenti
    ADD CONSTRAINT studenti_id_grupa_fkey FOREIGN KEY (id_grupa) REFERENCES public.grupe(id);


-- Completed on 2026-01-07 01:20:53

--
-- PostgreSQL database dump complete
--

\unrestrict Aay2OYDfxiv2aDvtf8dS6ZdJrwn062ghthQkzDxf0VeBUXV6lYnp3T8swudubKF

