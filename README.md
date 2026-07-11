# KAMI Creative Studio

Open, credential-free creator UI for a CID-linked Murakumo pipeline:

```text
brief / reference
  → 3D model (TRELLIS / Hunyuan3D)
  → humanoid rig (UniRig / explicit heuristic)
  → motion (validated EDN clip on the Mac mini control fleet)
  → music (ACE-Step / Stable Audio)
  → kami.creative-project/v1 manifest
```

Open the public Studio: <https://kotoba-lang.github.io/kami-creative-studio/>

## What is live

- Responsive, accessible project workspace
- Realtime local/generated VRM and GLB preview with camera controls and animation playback
- Integrated `kotoba-lang/kisekae` character edit operations for parts, materials, expressions and motion
- Independent CLJC trait-composition domain with one asset per slot and deterministic seeded randomization
- iPad-first single-window workspace: Library sidebar, Character Canvas, Inspector and Motion/Music Timeline in one viewport
- Murakumo progress polling and automatic preview when a generated artifact URL arrives
- Build-generated `KAMI Prism` sample project and embedded-buffer glTF, loaded into the viewer on startup
- Real VRM 1.0 humanoid display using pixiv's redistribution-permitted constraint sample, loaded from its upstream source URL
- Offline project planning and browser-local persistence
- JSON manifest preview, copy and download
- Explicit Murakumo endpoint submission with visible success/failure state
- No credentials, API keys, analytics or third-party scripts
- GitHub Pages deployment on every `main` update

Heavy model, rig and music inference requires a configured remote GPU capability. The Mac mini fleet controls the job graph and owns bounded EDN motion generation/validation; the UI never claims that CUDA-only models execute locally.

## Project contract

The generated document is `kami.creative-project/v1`. Every stage declares its modality, model, input relationship, output kinds and required execution capability. `noSilentFallback` is always true.

## Local development

```sh
npm ci
npm run release
python3 -m http.server 4173 --directory public
```

Then open `http://localhost:4173`. The authored UI is ClojureScript Hiccup/Reagent. Styling is extracted from `shadow.css/css` forms; the GitHub Pages shell is generated from Hiccup during release. There are no hand-authored HTML or CSS files.

The sample source is `resources/samples/kami-sample.edn`. `npm run release` deterministically generates `public/samples/kami-sample.gltf` and its project manifest; generated files are not committed.

The real-character demo references pixiv's `VRM1_Constraint_Twist_Sample.vrm` directly from the `pixiv/three-vrm` repository. Its embedded VRM metadata permits redistribution and identifies pixiv Inc. as author; KAMI does not copy the binary into this repository.

## Browser runtime boundary

- `kotoba-lang/webgpu` is the canonical browser GPU executor: CLJS calls the JavaScript WebGPU API directly (`navigator.gpu`, `requestAdapter`, `requestDevice`) and consumes EDN render-IR. It does not require Rust or Wasm.
- Kotoba Wasm remains the sandboxed guest-logic/capability layer. It can produce or transform scene intent, while the browser host executes rendering.
- This Studio currently uses `model-viewer` for portable glTF/VRM geometry preview. Direct `kami.webgpu` rendering is a separate opt-in renderer path, not falsely reported as active here.

## Workspace UX

The product is a single-window creative app rather than a sequence of web pages. Its information architecture follows Apple HIG patterns—persistent toolbar, sidebar, content canvas, inspector and bottom timeline—with 44px minimum primary controls. At the iPad landscape baseline (1024×768), the document has no page-level overflow; dense panels scroll internally without moving the canvas.

## Design reference

The asset-pack separation and point-and-click composition UX are independently designed with M3-org/CharacterStudio as a product reference. No CharacterStudio source code or assets are copied; KAMI's implementation is CLJC/CLJS Hiccup with shadow-css and its own manifest contract.

## License

MIT
