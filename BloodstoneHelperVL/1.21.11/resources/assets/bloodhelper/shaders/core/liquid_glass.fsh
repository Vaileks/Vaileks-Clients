#version 330

// Optical profile and normal/Snell displacement adapted from:
// Hla-aung/liquid-glass, liquid-glass/engine.ts, MIT, Copyright (c) 2026 Htet Lin Aung,
// commit 16e6c369a3f3d17857f830268a3e8713665cf7ab.
// Rounded lens height, crisp-rim blur and directional reflection adapted from:
// ybouane/liquidglass, src/shaders.ts, MIT,
// commit 59af227103795f06421a96e2c7d3dceb081decd2.
// Full upstream notices are bundled with BLOODHELPER.

#moj_import <minecraft:dynamictransforms.glsl>

flat in float reglassProfile;
uniform sampler2D Sampler0;
in vec4 vertexColor;
in vec2 localPosition;
flat in vec4 shape;
flat in float refraction;
flat in float menuMaterial;
flat in float materialDensity;
flat in float readability;
out vec4 fragColor;

vec3 linearColor(vec3 color) {
    return mix(color / 12.92, pow((color + 0.055) / 1.055, vec3(2.4)), step(vec3(0.04045), color));
}
vec3 displayColor(vec3 color) {
    color = max(color, vec3(0.0));
    return mix(color * 12.92, 1.055 * pow(color, vec3(1.0 / 2.4)) - 0.055, step(vec3(0.0031308), color));
}

float roundedDistance(vec2 position, vec2 halfSize, float radius) {
    vec2 q = abs(position) - halfSize + radius;
    return length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - radius;
}

// Convex circular cross-section shared by the referenced implementations.
// Finite differences keep its near-vertical outer tangent well behaved.
float lensHeight(float inside, float depth) {
    float d = clamp(inside, 0.0, depth);
    return sqrt(max(d * (2.0 * depth - d), 0.0));
}

vec3 dispersedSample(vec2 uv, vec2 dispersion, float lod) {
    return vec3(textureLod(Sampler0, uv + dispersion, lod).r,
                textureLod(Sampler0, uv, lod).g,
                textureLod(Sampler0, uv - dispersion, lod).b);
}

// Embedded ReGlass optics, adapted from RedxAx/ReGlass liquid_glass_gui.fsh.
// MIT, Copyright (c) 2025 ReStudio By RedxAx.
// Pinned source: 352a67f8ccfbfaa709f60cc354d26ca4a5f3af78.
// See META-INF/licenses/REGLASS-LICENSE.txt. Uses BloodHelper's one shared
// framebuffer capture/blur and per-quad clipping instead of an extra compositor.
vec2 reglassNormal(vec2 p, vec2 halfSize, float radius) {
    vec2 w = abs(p) - (halfSize - radius);
    vec2 q = max(w, vec2(0.0));
    float len = length(q);
    vec2 normal = max(w.x, w.y) > 0.0 ? q / max(len, 0.000001)
            : (w.x > w.y ? vec2(1.0, 0.0) : vec2(0.0, 1.0));
    return sign(p) * normal;
}
vec3 embeddedReGlass(vec2 halfSize, float radius, float inside, vec2 pixelsPerUnit, vec4 tint) {
    vec2 texel = 1.0 / vec2(textureSize(Sampler0, 0));
    vec2 uv = gl_FragCoord.xy * texel;
    float unit = min(pixelsPerUnit.x, pixelsPerUnit.y);
    float insidePixels = inside * unit;
    vec2 normal = reglassNormal(localPosition, halfSize, radius) * vec2(1.0, -1.0);
    float thickness = max(1.0, min(13.15 * unit, min(halfSize.x, halfSize.y) * unit));
    float xR = 1.0 - insidePixels / thickness;
    float thetaI = asin(pow(clamp(xR, 0.0, 1.0), 2.0));
    float thetaT = asin(sin(thetaI) / 1.4);
    float edgeFactor = insidePixels < thickness ? -tan(thetaT - thetaI) : 0.0;
    float strength = refraction / 3.0;
    vec2 offset = -normal * edgeFactor * 0.08 * strength * vec2(texel.x / texel.y, 1.0);
    // Bound displacement at screen edges and unusually large user settings.
    vec2 limit = texel * 24.0 * unit * strength;
    offset = clamp(offset, -limit, limit);
    float lod = shape.w > 0.5 ? float(GLASS_BLUR_LOD) : 0.0;
    vec2 sampleUV = clamp(uv + offset, texel * 0.5, vec2(1.0) - texel * 0.5);
    vec3 blurred = textureLod(Sampler0, sampleUV, lod).rgb;
    vec3 dispersed = blurred;
#ifndef GLASS_LOW
    if (edgeFactor > 0.0 && strength > 0.0) {
        float dispersion = 7.0 * strength;
        dispersed.r = textureLod(Sampler0, clamp(uv + offset * (1.0 + 0.015 * dispersion), texel * 0.5, vec2(1.0) - texel * 0.5), lod).r;
        dispersed.b = textureLod(Sampler0, clamp(uv + offset * (1.0 - 0.015 * dispersion), texel * 0.5, vec2(1.0) - texel * 0.5), lod).b;
    }
#endif
    float tintAlpha = mix(0.68, 0.78, menuMaterial) * materialDensity;
    vec3 color = mix(dispersed, tint.rgb, tintAlpha * 0.8);
    float fresnel = clamp(pow(max(0.0, 1.0 - insidePixels / 1500.0 * pow(500.0 / (39.71 * unit), 2.0) + 0.2), 5.0), 0.0, 1.0);
    color = mix(color, mix(vec3(1.0), tint.rgb, tintAlpha * 0.5), fresnel * 0.2 * 0.7);
    float glare = clamp(pow(max(0.0, 1.0 - insidePixels / 1500.0 * pow(500.0 / (30.0 * unit), 2.0) + 0.2), 5.0), 0.0, 1.0);
    color = mix(color, mix(blurred, tint.rgb, tintAlpha * 0.5), 0.25 * glare);
    if (readability > 0.5) {
        vec3 linear = linearColor(color);
        float luma = dot(linear, vec3(0.2126, 0.7152, 0.0722));
        if (readability < 1.5) linear *= min(1.0, 0.065 / max(luma, 0.0001));
        else linear = mix(linear, vec3(1.0), clamp((0.63 - luma) / max(1.0 - luma, 0.0001), 0.0, 1.0));
        color = displayColor(linear);
    }
    return clamp(color, 0.0, 1.0);
}

void main() {
    vec2 halfSize = shape.xy;
    float radius = shape.z;
    float distanceToEdge = roundedDistance(localPosition, halfSize, radius);
    float inside = max(-distanceToEdge, 0.0);
    float aa = max(fwidth(distanceToEdge), 0.01);
    vec2 pixelsPerUnit = 1.0 / max(fwidth(localPosition), vec2(0.001));
    float coverage = 1.0 - smoothstep(-aa * 0.5, aa * 0.5, distanceToEdge);
    if (coverage <= 0.0) discard;
    if (reglassProfile > 0.5) {
        vec4 tint = vertexColor * ColorModulator;
        fragColor = vec4(embeddedReGlass(halfSize,radius,inside,pixelsPerUnit,tint),tint.a*coverage);
        return;
    }

    // Lens depth is independent of corner radius. Menus retain a narrow bevel;
    // compact HUD panels get the same optical edge at their own scale.
    float lensDepth = min(min(halfSize.x, halfSize.y), clamp(radius * 0.8 + 2.0, 4.0, 13.0));
    float epsilon = max(0.35, min(aa * 0.6, 1.0));
    // Only the bevel has a changing normal. The flat interior is exact and
    // skips four distance/sqrt evaluations for most pixels in a large panel.
    vec3 normal = vec3(0.0, 0.0, 1.0);
    vec2 heightGradient = vec2(0.0);
    if (inside < lensDepth + epsilon) {
    float hR = lensHeight(-roundedDistance(localPosition + vec2(epsilon, 0.0), halfSize, radius), lensDepth);
    float hL = lensHeight(-roundedDistance(localPosition - vec2(epsilon, 0.0), halfSize, radius), lensDepth);
    float hD = lensHeight(-roundedDistance(localPosition + vec2(0.0, epsilon), halfSize, radius), lensDepth);
    float hU = lensHeight(-roundedDistance(localPosition - vec2(0.0, epsilon), halfSize, radius), lensDepth);
    heightGradient = vec2(hR - hL, hD - hU) / (2.0 * epsilon);
    normal = normalize(vec3(-heightGradient, 1.0));
    }
    float height = lensHeight(inside, lensDepth);
    float edge = 1.0 - smoothstep(0.0, lensDepth, inside);

    // ybouane/liquidglass: a biconvex lens refracts at entry and exit.
    // Clamp the near-vertical tangent so small HUD panels stay readable.
    const float ior = 1.5;
    float normalizedThickness = height / max(lensDepth, 1.0);
    vec2 slope = clamp(heightGradient, vec2(-2.5), vec2(2.5));
    vec2 entryRefraction = slope * (1.0 - 1.0 / ior);
    float strength = refraction * 0.1;
    vec2 refractedPixels = entryRefraction * (2.0 + normalizedThickness * 0.5) * strength * 8.0;
    vec2 centerDirection = -localPosition / max(halfSize, vec2(1.0));
    refractedPixels += centerDirection * strength * 1.5 * smoothstep(0.0, lensDepth, inside);
    float maxShift = min(18.0, lensDepth * 1.4);
    refractedPixels = clamp(refractedPixels, vec2(-maxShift), vec2(maxShift));
    vec2 texel = 1.0 / vec2(textureSize(Sampler0, 0));
    // Local GUI Y points down; framebuffer coordinates and its texture point up.
    // Flip the optical displacement, never the complete captured image.
    vec2 localToUV = pixelsPerUnit * texel * vec2(1.0, -1.0);
    vec2 uv = gl_FragCoord.xy * texel + refractedPixels * localToUV;
    vec2 dispersion = normal.xy * localToUV * min(0.34, refraction * 0.045) * edge;
#ifdef GLASS_LOW
    vec3 sharp = textureLod(Sampler0, uv, 0.0).rgb;
    vec3 blurred = shape.w > 0.5 ? textureLod(Sampler0, uv, float(GLASS_BLUR_LOD)).rgb : sharp;
#else
    // Dispersion is exactly zero on the flat interior: one RGB sample there
    // replaces three identical texture fetches without changing its colour.
    vec3 sharp = edge > 0.0 ? dispersedSample(uv, dispersion, 0.0) : textureLod(Sampler0, uv, 0.0).rgb;
    vec3 blurred = sharp;
    if (shape.w > 0.5)
        blurred = edge > 0.0 ? dispersedSample(uv, dispersion, float(GLASS_BLUR_LOD))
                            : textureLod(Sampler0, uv, float(GLASS_BLUR_LOD)).rgb;
#endif
    // Keep the world legible through HUD glass; menus can soften it a little more.
    float blurMix = step(0.5, shape.w) * (1.0 - edge * 0.15) * mix(0.58, 0.90, menuMaterial);
    vec3 background = mix(linearColor(sharp), linearColor(blurred), blurMix);

    // Absorption/reflection/light accumulation happen in linear space. Clear
    // HUD glass preserves the scene while its tint supports text. Alpha controls visibility,
    // including existing HUD enter/exit animation, independently of material.
    vec4 tint = vertexColor * ColorModulator;
    vec3 tintLinear = linearColor(tint.rgb);
    float transmission = mix(0.88, 0.68, menuMaterial);
    transmission *= mix(1.0, 1.12, edge);
    vec3 color = background * transmission + tintLinear * mix(0.10, 0.25, menuMaterial);
    float normalView = clamp(normal.z, 0.0, 1.0);
    float f0 = pow((ior - 1.0) / (ior + 1.0), 2.0);
    float fresnel = f0 + (1.0 - f0) * pow(1.0 - normalView, 5.0);
    vec3 view = vec3(0.0, 0.0, 1.0);
    vec3 keyLight = normalize(vec3(-0.55, -0.80, 1.0));
    vec3 fillLight = normalize(vec3(0.60, 0.65, 0.85));
    float keySpec = pow(max(dot(normal, normalize(keyLight + view)), 0.0), 64.0);
    float fillSpec = pow(max(dot(normal, normalize(fillLight + view)), 0.0), 90.0);
    float edgeLighting = 0.20 + 0.80 * max(dot(normal, keyLight), 0.0);
    // A straight edge has the same normal over its entire length. Strong
    // constant reflections there become a grey header stripe, especially after
    // conversion from linear light. Keep reflections soft on straight edges
    // and let the rounded corners carry the visible glass highlights.
    vec2 cornerDelta = max(abs(localPosition) - (halfSize - vec2(radius)), vec2(0.0));
    float cornerMask = smoothstep(0.0, max(radius * 0.35, aa), min(cornerDelta.x, cornerDelta.y));
    float reflectionWeight = mix(0.08, 0.20, cornerMask);
    float rim = 1.0 - smoothstep(aa * 0.15, aa * 0.65, inside);
    color += vec3(0.12, 0.14, 0.18) * fresnel * edgeLighting * reflectionWeight;
    color += vec3(0.16, 0.17, 0.19) * keySpec * edge * reflectionWeight;
    color += vec3(0.09, 0.08, 0.08) * fillSpec * edge * reflectionWeight;
    color += vec3(0.014, 0.017, 0.022) * rim * reflectionWeight;
    // A broad cool reflection and a narrow lower glint keep the lens visible in motion.
    float broad = pow(max(dot(normal,normalize(vec3(0.1,-0.3,1.0))),0.0),6.0)*edge;
    float lower = pow(max(dot(normal,normalize(vec3(0.0,0.9,1.4))),0.0),120.0)*edge;
    color += vec3(0.009,0.010,0.014)*broad + vec3(0.012)*lower;
    // Material opacity blends the sampled backdrop, never the sharp destination.
    // Otherwise ordinary alpha compositing reintroduces unblurred leaves/sky through
    // the lens. Final alpha is only geometric coverage and enter/exit visibility.
    float absorption = 1.0 - pow(1.0 - materialDensity, 2.0);
    color = mix(background, color, absorption);
    // Reuse the already sampled backdrop. No readback, extra texture lookup or blur pass.
    // Keep light text above dark backgrounds and dark text above light backgrounds.
    if (readability > 0.5) {
        float luma = dot(color, vec3(0.2126, 0.7152, 0.0722));
        if (readability < 1.5) color *= min(1.0, 0.065 / max(luma, 0.0001));
        else color = mix(color, vec3(1.0), clamp((0.63 - luma) / max(1.0 - luma, 0.0001), 0.0, 1.0));
    }
    fragColor = vec4(clamp(displayColor(color), 0.0, 1.0), tint.a * coverage);
}
