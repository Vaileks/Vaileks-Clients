import os
import hashlib
import json
import struct
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SOURCE = Path(os.environ.get('BLOODHELPER_ORIGINAL_DIR', 'originals')) / 'BloodHelper-1.3.8+1.21.11.jar'
DEST = ROOT / 'outputs/BloodHelper-1.3.8+1.21.11-privacy.jar'
EXPECTED = 'c7f86bfcbaa2c51006c26c6e10e6d477ee68c9c3db7cec0a23f6e8833bb75687'
VARIANT = sys.argv[1] if len(sys.argv) > 1 else 'original'
variants = {
    'lunar': ('BloodHelper-1.3.8+1.21.11-lunarclient', '8a5506594918bd15e8ca134d306521ff2bfbccfedd26c4b5d24c1c3df0a71724'),
    '26.3': ('BloodHelper-1.3.8+26.3', '930064c8a2e1b7894793fe063c7f446e67937bb49cca1e9cc6ae04631a93266e'),
}
if VARIANT in variants:
    stem, EXPECTED = variants[VARIANT]
    SOURCE = Path(os.environ.get('BLOODHELPER_ORIGINAL_DIR', 'originals')) / (stem + '.jar')
    DEST = ROOT / 'outputs' / (stem + '-privacy.jar')
assert hashlib.sha256(SOURCE.read_bytes()).hexdigest() == EXPECTED

def u2(data, offset):
    return struct.unpack_from('>H', data, offset)[0]

def u4(data, offset):
    return struct.unpack_from('>I', data, offset)[0]

def inspect(data):
    assert data[:4] == b'\xca\xfe\xba\xbe'
    cp = {}
    offset = 10
    index = 1
    while index < u2(data, 8):
        tag = data[offset]
        offset += 1
        if tag == 1:
            length = u2(data, offset)
            offset += 2
            cp[index] = data[offset:offset + length].decode('utf-8', errors='replace')
            offset += length
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            offset += 4
        elif tag in (5, 6):
            offset += 8
            index += 1
        elif tag in (7, 8, 16, 19, 20):
            offset += 2
        elif tag == 15:
            offset += 3
        else:
            raise ValueError(tag)
        index += 1
    offset += 6
    offset += 2 + 2 * u2(data, offset)
    fields = u2(data, offset)
    offset += 2
    for _ in range(fields):
        attributes = u2(data, offset + 6)
        offset += 8
        for _ in range(attributes):
            offset += 6 + u4(data, offset + 2)
    methods = u2(data, offset)
    offset += 2
    result = []
    for _ in range(methods):
        name, descriptor = cp[u2(data, offset + 2)], cp[u2(data, offset + 4)]
        attributes = u2(data, offset + 6)
        offset += 8
        for _ in range(attributes):
            start = offset
            length = u4(data, offset + 2)
            offset += 6
            if cp[u2(data, start)] == 'Code':
                result.append(dict(name=name, descriptor=descriptor, start=start,
                                   end=offset + length, max_locals=u2(data, offset + 2),
                                   code=data[offset + 8:offset + 8 + u4(data, offset + 4)]))
            offset += length
    return result

changes = []

def patch(data, path, replacements):
    edits = []
    found = set()
    for method in inspect(data):
        key = (method['name'], method['descriptor'])
        if key not in replacements:
            continue
        found.add(key)
        code = replacements[key](method['code'])
        # New methods are straight-line code: no exception table, stack maps or stale line tables.
        body = struct.pack('>HHI', 4, method['max_locals'], len(code)) + code + b'\0\0\0\0'
        start = method['start']
        replacement = data[start:start + 2] + struct.pack('>I', len(body)) + body
        edits.append((start, method['end'], replacement))
        changes.append(dict(file=path, method=key[0], descriptor=key[1],
                            old_code_bytes=len(method['code']), new_code_hex=code.hex()))
    assert found == set(replacements), (path, found, set(replacements))
    for start, end, replacement in sorted(edits, reverse=True):
        data = data[:start] + replacement + data[end:]
    return data

def local_init(code):
    # Retain local CommunityFeed and CustomModels setup, without device/cache/service setup.
    assert code[31:34] == bytes.fromhex('b8006f' if VARIANT == '26.3' else 'b800aa')
    assert code[62:65] == bytes.fromhex('b80089' if VARIANT == '26.3' else 'b800c0')
    return code[31:49] + code[62:65] + b'\xb1'

base = 'ru/bloodhelper/client/'
targets = {
    base + 'access/ClientControlService.class': {
        ('initialize', '(Ljava/lang/String;)V'): local_init,
        ('poll', '()V'): lambda _: b'\xb1',
        ('pollDevice', '()V'): lambda _: b'\xb1',
    },
    base + 'access/ServiceTransport.class': {
        ('request', '(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/google/gson/JsonObject;I)[B'):
            # throw new IOException("Service unavailable"); No network call can be made.
            lambda _: bytes.fromhex('bb005f591300d5b70063bf' if VARIANT == '26.3' else 'bb005e59130116b7009ebf'),
    },
    base + 'music/MusicSearchService.class': {
        ('connectYandex', '(Lru/bloodhelper/client/music/MusicSearchResult;)V'): lambda _: b'\xb1',
        ('lambda$connectYandex$0', '(JLru/bloodhelper/client/music/MusicSearchResult;)V'): lambda _: b'\xb1',
    },
}
stub = b'''# Yandex background connection disabled in this privacy build.\n[Console]::Out.WriteLine('{"status":"unsupported"}')\n[Console]::Out.Flush()\n'''
with zipfile.ZipFile(SOURCE) as src, zipfile.ZipFile(DEST, 'w') as dst:
    assert not any(n.upper().endswith(('.SF', '.RSA', '.DSA', '.EC')) for n in src.namelist())
    for info in src.infolist():
        data = src.read(info.filename)
        if info.filename in targets:
            data = patch(data, info.filename, targets[info.filename])
        if info.filename == 'bloodhelper/music-yandex.ps1':
            data = stub
            changes.append(dict(file=info.filename, action='Replace with inert unsupported-status script'))
        dst.writestr(info, data)

with zipfile.ZipFile(SOURCE) as src, zipfile.ZipFile(DEST) as dst:
    assert dst.testzip() is None
    assert src.namelist() == dst.namelist()
    changed = [n for n in src.namelist() if src.read(n) != dst.read(n)]
    assert set(changed) == set(targets) | {'bloodhelper/music-yandex.ps1'}
    for path, replacements in targets.items():
        methods = {(m['name'], m['descriptor']): m for m in inspect(dst.read(path))}
        originals = {(m['name'], m['descriptor']): m for m in inspect(src.read(path))}
        for key, transform in replacements.items():
            assert methods[key]['code'] == transform(originals[key]['code'])

report = dict(source_sha256=EXPECTED, output_sha256=hashlib.sha256(DEST.read_bytes()).hexdigest(),
              changed_entries=changed, changes=changes)
(ROOT / ('work/bloodhelper/patch-manifest-' + VARIANT + '.json')).write_text(json.dumps(report, indent=2))
print(json.dumps(report, indent=2))
