import bpy
import os
import logging

bpy.ops.object.delete(use_global=False)

PATH_SVG_SOURCE = "svg"
PATH_MODEL_TARGET = "models"
PATH_LABELS = "labels.txt"

with open(PATH_LABELS) as f:
    labels = {x.strip() for x in f.readlines()}

for i, path in enumerate(os.listdir(PATH_SVG_SOURCE)):
    if not path.endswith(".svg"):
        continue

    label = os.path.splitext(path)[0]

    if label not in labels:
        continue

    source_path = os.path.join(PATH_SVG_SOURCE, path)
    target_path = os.path.join(PATH_MODEL_TARGET, path.replace(".svg", ".fbx"))

    C = bpy.context
    names_pre_import = set([ o.name for o in C.scene.objects ])
   
    bpy.ops.import_curve.svg(filepath=source_path, filter_glob="*.svg")
    
    bpy.ops.object.select_all(action='DESELECT')
    bpy.ops.object.select_by_type(type='CURVE')
    bpy.context.view_layer.objects.active = bpy.data.objects["Curve"]

    for nr, obj in enumerate(bpy.context.selected_objects):
        obj.data.extrude = 0.01

    bpy.context.view_layer.objects.active = bpy.data.objects["Curve"]

    bpy.ops.export_scene.fbx(filepath=target_path,check_existing=True, axis_forward='Y', axis_up='Z', use_selection=False)
    bpy.ops.object.select_by_type(type='CURVE')
    bpy.ops.object.delete()