#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
out vec2 iconPosition;
out vec4 vertexColor;

void main() {
    // GuiGraphics.fill normalizes x0/y0 to the maximum corner. Rectangle
    // vertices are BR, TR, TL, BL; each batched rectangle has four vertices.
    int corner = gl_VertexID & 3;
    iconPosition = vec2(corner < 2 ? 24.0 : 0.0, (corner == 0 || corner == 3) ? 24.0 : 0.0);
    vertexColor = Color;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
