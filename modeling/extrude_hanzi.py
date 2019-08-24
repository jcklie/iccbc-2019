import bpy
import os
import logging

bpy.ops.object.delete(use_global=False)

PATH_SVG_SOURCE = "/home/klie/git/yangtao/modeling/svg"
PATH_MODEL_TARGET = "/home/klie/git/yangtao/modeling/models"

for i, path in enumerate(os.listdir(PATH_SVG_SOURCE)):
    if not path.endswith(".svg"):
        continue

    source_path = os.path.join(PATH_SVG_SOURCE, path)
    target_path = os.path.join(PATH_MODEL_TARGET, path.replace(".svg", ".obj"))

    C = bpy.context
    names_pre_import = set([ o.name for o in C.scene.objects ])
   
    bpy.ops.import_curve.svg(filepath=source_path, filter_glob="*.svg")
    
    bpy.ops.object.select_all(action='DESELECT')
    bpy.ops.object.select_by_type(type='CURVE')
    bpy.context.view_layer.objects.active = bpy.data.objects["Curve"]

    for nr, obj in enumerate(bpy.context.selected_objects):
        obj.data.extrude = 0.01

    bpy.context.view_layer.objects.active = bpy.data.objects["Curve"]

    bpy.ops.export_scene.obj(filepath=target_path,check_existing=True, axis_forward='Y', axis_up='Z', filter_glob="*.3ds", use_selection=False)
    bpy.ops.object.select_by_type(type='CURVE')
    bpy.ops.object.delete()

    if i > 100:
        break
