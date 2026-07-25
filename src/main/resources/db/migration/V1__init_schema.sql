CREATE TABLE staff (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    nama          VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'STAFF' CHECK (role IN ('ADMIN','STAFF')),
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE pasien (
    id              BIGSERIAL PRIMARY KEY,
    no_rekam_medis  VARCHAR(20)  NOT NULL UNIQUE,
    nama            VARCHAR(100) NOT NULL,
    nik             VARCHAR(16)  NOT NULL UNIQUE,
    tanggal_lahir   DATE NOT NULL,
    jenis_kelamin   VARCHAR(1)   NOT NULL CHECK (jenis_kelamin IN ('L','P')),
    alamat          TEXT,
    no_telepon      VARCHAR(20),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE dokter (
    id            BIGSERIAL PRIMARY KEY,
    nama          VARCHAR(100) NOT NULL,
    spesialisasi  VARCHAR(100) NOT NULL,
    no_str        VARCHAR(30)  NOT NULL UNIQUE,
    no_telepon    VARCHAR(20),
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE obat (
    id          BIGSERIAL PRIMARY KEY,
    kode_obat   VARCHAR(20)  NOT NULL UNIQUE,
    nama        VARCHAR(100) NOT NULL,
    satuan      VARCHAR(20)  NOT NULL,
    harga       NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (harga >= 0),
    stok        INTEGER NOT NULL DEFAULT 0 CHECK (stok >= 0),
    deskripsi   TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE reservasi (
    id                 BIGSERIAL PRIMARY KEY,
    pasien_id          BIGINT NOT NULL REFERENCES pasien(id),
    dokter_id          BIGINT NOT NULL REFERENCES dokter(id),
    tanggal_reservasi  DATE NOT NULL,
    jam_reservasi      TIME NOT NULL,
    keluhan            TEXT,
    status             VARCHAR(10) NOT NULL DEFAULT 'MENUNGGU'
                         CHECK (status IN ('MENUNGGU','SELESAI','BATAL')),
    created_at         TIMESTAMP NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_reservasi_pasien ON reservasi(pasien_id);
CREATE INDEX idx_reservasi_dokter ON reservasi(dokter_id);
CREATE INDEX idx_reservasi_tanggal ON reservasi(tanggal_reservasi);

-- cegah 1 dokter double-booked pada slot yang sama (reservasi batal diabaikan)
CREATE UNIQUE INDEX uq_reservasi_slot_dokter
    ON reservasi (dokter_id, tanggal_reservasi, jam_reservasi)
    WHERE status <> 'BATAL';

CREATE TABLE rekam_medis (
    id               BIGSERIAL PRIMARY KEY,
    reservasi_id     BIGINT REFERENCES reservasi(id),
    pasien_id        BIGINT NOT NULL REFERENCES pasien(id),
    dokter_id        BIGINT NOT NULL REFERENCES dokter(id),
    tanggal_periksa  DATE NOT NULL DEFAULT CURRENT_DATE,
    diagnosis        TEXT NOT NULL,
    catatan          TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_rekam_medis_pasien ON rekam_medis(pasien_id);
CREATE INDEX idx_rekam_medis_dokter ON rekam_medis(dokter_id);
CREATE INDEX idx_rekam_medis_tanggal ON rekam_medis(tanggal_periksa);

CREATE TABLE rekam_medis_obat (
    id              BIGSERIAL PRIMARY KEY,
    rekam_medis_id  BIGINT NOT NULL REFERENCES rekam_medis(id) ON DELETE CASCADE,
    obat_id         BIGINT NOT NULL REFERENCES obat(id),
    jumlah          INTEGER NOT NULL CHECK (jumlah > 0),
    dosis           VARCHAR(100),
    UNIQUE (rekam_medis_id, obat_id)
);

CREATE INDEX idx_rmo_obat ON rekam_medis_obat(obat_id);
