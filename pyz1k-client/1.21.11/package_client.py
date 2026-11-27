#!/usr/bin/env python3
"""Merge remapped additions into the unchanged, hash-verified original client."""
from pathlib import Path
import hashlib
import json
import zipfile

ROOT = Path(__file__).resolve().parent
VERSION = '1.5.0-vaileks'
BASE_SHA256 = 'ab637a8e0378baebfcc9d68d4d4e211e3504aa16d0728372757aa244253ee063'
base = ROOT / 'libs/pyz1k-base.jar'
additions = ROOT / f'build/libs/pyz1k-additions-{VERSION}.jar'
assert hashlib.sha256(base.read_bytes()).hexdigest() == BASE_SHA256, 'Unexpected original client'
dest = ROOT / 'dist/pyz1k-client.jar'
dest.parent.mkdir(parents=True, exist_ok=True)

with zipfile.ZipFile(base) as original, zipfile.ZipFile(additions) as extra:
    entries = {n: original.read(n) for n in original.namelist() if not n.endswith('/')}
    for n in extra.namelist():
        if n.endswith('/') or n in ('fabric.mod.json', 'META-INF/MANIFEST.MF'):
            continue
        assert n not in entries, f'Unintended replacement: {n}'
        entries[n] = extra.read(n)
    metadata = json.loads(entries['fabric.mod.json'])
    metadata['version'] = VERSION
    metadata['name'] = 'pyz1k-client'
    metadata['description'] = ('Pyz1k с дополнениями Vaileks: '
                               'ItemSkin, BetterNameVisibility, BloodSwordCooldown.')
    metadata['authors'] = list(dict.fromkeys(metadata.get('authors', []) + ['Vaileks']))
    metadata.setdefault('entrypoints', {}).setdefault('client', []).append('pyz1k.extensions.ExtensionClient')
    metadata.setdefault('mixins', []).extend(['pyz1k-extensions.mixins.json','pyz1k-pvp.mixins.json'])
    entries['fabric.mod.json'] = (json.dumps(metadata, ensure_ascii=False, indent=2) + '\n').encode()
    manifest = entries['META-INF/MANIFEST.MF'].decode().rstrip() + f'\r\nImplementation-Version: {VERSION}\r\n\r\n'
    entries['META-INF/MANIFEST.MF'] = manifest.encode()
    with zipfile.ZipFile(dest, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as output:
        for n, content in sorted(entries.items()):
            info = zipfile.ZipInfo(n, date_time=(2026, 11, 11, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            output.writestr(info, content)

with zipfile.ZipFile(dest) as result:
    assert result.testzip() is None
    assert not any('/smoke/' in n for n in result.namelist())
    assert not any(n.startswith('ru/bloodstone/lootglow/') for n in result.namelist())
    for mixins in metadata['mixins']:
        config = json.loads(result.read(mixins))
        prefix = config.get('package', '').replace('.', '/') + '/'
        for name in config.get('client', []) + config.get('mixins', []):
            assert prefix + name.replace('.', '/') + '.class' in result.namelist(), (mixins, name)
    print(dest)
    print('SHA256:', hashlib.sha256(dest.read_bytes()).hexdigest())
    print('All original class bytes preserved; additions and mixin entries verified.')

import subprocess, os
asm = (ROOT.parent / 'client-update-tools/asm-path.txt').read_text().strip()
java = Path(os.environ['JAVA_HOME']) / 'bin/java'
subprocess.run([str(java), '-cp', asm + os.pathsep + str(ROOT.parent/'client-update-tools'), 'StripItemsRender', str(dest)], check=True)
