remove_attributes = {
    "createTimestamp",
    "creatorsName",
    "entryCSN",
    "entryDN",
    "entryParentId",
    "entryUUID",
    "hasSubordinates",
    "nbChildren",
    "nbSubordinates",
    "structuralObjectClass",
    "subschemaSubentry",
    "pwdHistory",
    "modifiersName",
    "modifyTimestamp",
    "1.3.6.1.4.1.18060.0.4.1.2.51"
}

with open("export.ldif", "r", encoding="utf-8") as f:
    lines = f.readlines()

cleaned = []
skip_next = False

for line in lines:

    # continuation line from removed attribute
    if line.startswith(" "):
        if skip_next:
            continue
        else:
            cleaned.append(line)
            continue

    attribute = line.split(":", 1)[0]

    if attribute in remove_attributes:
        skip_next = True
        continue

    skip_next = False
    cleaned.append(line)


with open("clean_export.ldif", "w", encoding="utf-8") as f:
    f.writelines(cleaned)