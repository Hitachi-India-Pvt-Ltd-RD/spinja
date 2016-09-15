sudo rm -r build
sudo rm spinja.jar
cd src
ant build
cd ..


mkdir FILES
bash spinja.sh -DSECONDARYBTREE $1
rm -r FILES
