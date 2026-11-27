# Chat Patches integration

BloodHelper 1.2 integrates with the original, separately installed **Chat Patches** by **OBro1961**.

- Upstream: https://github.com/mrbuilder1961/ChatPatches
- Downloads: https://modrinth.com/mod/chatpatches
- Upstream license: LGPL-3.0-only (see the original distribution).
- Reference inspected: commit `987cf24502ad737c83959fd5f1297a2fd42165d7`.
- The installed 1.21.11 release `8.0-alpha.11` was also inspected statically to verify the optional configuration/restoration API.

No Chat Patches implementation is copied, shaded or bundled by this change. `ExternalChatPatches` is an optional bridge to its existing public configuration and restoration methods. `ChatTimestamps` reads the saved timestamp metadata from its component layout (timestamp, body, duplicate counter), preserving that layout for its native search/copy/logging features. The timestamp insertion is epoch milliseconds, including when the visible clock is disabled. Unknown restored dates are left unknown.

BloodHelper preserves Chat Patches timestamp slots and delegates duplicate collapsing to it, preventing two mods from wrapping or deleting the same message. When old setups disabled the native clock but kept BloodHelper timestamps enabled, BloodHelper fills the existing empty timestamp slot using the original saved date, without enabling native logging or wrapping the message again. The native history, hover dates, session separators, search and context-copy options are controlled through the original mod; other native settings remain available in its own screen. No logging settings are silently enabled on startup. Without Chat Patches, BloodHelper keeps its existing current-session tools and marks its own timestamp components so another compatible history mod can preserve their date.

Chat Patches and its declared dependencies must match the Minecraft version. This integration is also guarded when an unsupported Chat Patches configuration API is encountered; it still avoids adding a false current timestamp.
