//public void onStart() {
//        super.onStart();
//
//        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
//        .setQuery(mIncomeDatabase, Data.class)
//        .build();
//
//        adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
//
//public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.income_recycler_data, parent, false));
//        }
//
//protected void onBindViewHolder(MyViewHolder holder, int position, @NonNull Data model) {
//        holder.setAmmount(model.getAmount());
//        holder.setType(model.getType());
//        holder.setNote(model.getNote());
//        holder.setDate(model.getDate());
//        }
//        };
//        recyclerView.setAdapter(adapter);
//        }
//        }