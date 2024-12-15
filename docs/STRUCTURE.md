## Chunk

### Related Constants
`CHUNK_WIDTH` The length of the chunk on the X-axis (default: 32)
<br>
`CHUNK_HEIGHT` The length of the chunk on the Y-axis (default: 32)
<br>
`CHUNK_LENGTH` The length of the chunk on the Z-axis (default: 32)
<br>
### Overview
In Project Voxel, a chunk can be defined in two ways:
1. An instance of `MeshData`, which contains the chunk's mesh data.
   * Created in `MeshDataGenerator`
   * Used in `MeshGenerator` to create a mesh (see below)
2. An instance of `Mesh`, which contains the chunk's mesh.
   1. Created in `MeshGenerator`
   2. Used in `OpenGLThread` to render the world.