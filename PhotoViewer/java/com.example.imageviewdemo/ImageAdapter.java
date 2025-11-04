// ImageAdapter.java
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Bitmap> imageList;
    public ImageAdapter(List<Bitmap> imageList){ this.imageList = imageList; }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int pos) {
        holder.imageView.setImageBitmap(imageList.get(pos));
    }
    @Override public int getItemCount(){ return imageList.size(); }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageViewHolder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem);
        }
    }
}
