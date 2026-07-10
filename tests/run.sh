#!/bin/bash
# Run all scenarios in the correct order.
# Results are saved to the results/ folder as JSON for later analysis.

mkdir -p results

echo "========================================="
echo "1/5 SMOKE TEST"
echo "========================================="
k6 run --out json=results/smoke.json scenarios/smoke.js

echo ""
echo "========================================="
echo "2/5 AUTH STRESS TEST (LDAP bind)"
echo "========================================="
k6 run --out json=results/auth_stress.json scenarios/auth_stress.js

echo ""
echo "========================================="
echo "3/5 LOAD TEST (normal traffic)"
echo "========================================="
k6 run --out json=results/load.json scenarios/load.js

echo ""
echo "========================================="
echo "4/5 STRESS TEST (find breaking point)"
echo "========================================="
k6 run --out json=results/stress.json scenarios/stress.js

echo ""
echo "========================================="
echo "5/5 SPIKE TEST"
echo "========================================="
k6 run --out json=results/spike.json scenarios/spike.js

echo ""
echo "All tests complete. Results saved to results/"
