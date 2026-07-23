def get_entries(filename):
    with open(filename, "r", encoding="utf-8") as f:
        content = f.read()

    entries = content.strip().split("\n\n")

    return entries


def get_dn_depth(entry):
    for line in entry.splitlines():
        if line.startswith("dn:"):
            dn = line.replace("dn:", "").strip()

            # Count components in DN
            return len(dn.split(","))

    return 0


entries = get_entries("clean_export.ldif")

# Parents have fewer DN components, so they come first
entries.sort(key=get_dn_depth)

with open("sorted_export.ldif", "w", encoding="utf-8") as f:
    for entry in entries:
        f.write(entry.strip())
        f.write("\n\n")

print("Done: sorted_export.ldif created")